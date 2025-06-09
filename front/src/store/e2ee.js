/**
 * E2EE Pinia Store
 * Manages end-to-end encryption state and operations
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import e2eeManager from '@/utils/encryption'
import e2eeApiService from '@/services/e2eeApi'
import localMessageStorage from '@/utils/localMessageStorage'
import { ElMessage } from 'element-plus'
import { checkE2EEDataExists } from '@/utils/storage'

export const useE2EEStore = defineStore('e2ee', () => {
    // State
    const isEnabled = ref(false)
    const isInitialized = ref(false)
    const isLoading = ref(false)
    const publicKey = ref(null)
    const contactPublicKeys = ref(new Map()) // userId -> publicKey mapping
    const encryptionStatus = ref(new Map()) // conversationId -> encryption status
    const e2eeStatuses = ref(new Map()) // userId -> E2EE enabled status mapping
    const keysReady = ref(false) // Reactive trigger for key availability

    // Computed
    const isAvailable = computed(() => {
        // Use keysReady.value to make this reactive to key changes
        const available = keysReady.value && e2eeManager.isAvailable()
        return available
    })

    const canEncrypt = computed(() => {
        const result = isEnabled.value && isAvailable.value
        return result
    })

    /**
     * Initialize E2EE system
     * @returns {Promise<boolean>} - Success status
     */
    async function initialize() {
        if (isInitialized.value) {
            return true
        }

        try {
            isLoading.value = true

            // Check if E2EE data exists in localStorage first
            const e2eeDataStatus = checkE2EEDataExists()

            // Try to load existing keys from local storage
            const keysLoaded = await e2eeManager.loadKeysFromStorage()
            
            if (keysLoaded) {
                publicKey.value = await e2eeManager.getPublicKeyBase64()
                keysReady.value = true // Trigger reactivity
                
                // Get current E2EE settings from server
                try {
                    const settings = await e2eeApiService.getMyE2EESettings()
                    isEnabled.value = settings.e2eeEnabled || false
                } catch (error) {
                    console.warn('Could not fetch E2EE settings from server:', error)
                    // Check local storage for enabled status
                    isEnabled.value = localStorage.getItem('e2ee_enabled') === 'true'
                }
            } else {
                // Double-check if keys exist in storage but failed to load
                if (e2eeDataStatus.isComplete) {
                    console.warn('E2EE keys exist in storage but failed to load - possible corruption')
                    ElMessage.warning('检测到加密密钥但加载失败，请尝试重新生成密钥')
                }
                isEnabled.value = false
                keysReady.value = false // Ensure reactivity is correct
            }

            isInitialized.value = true
            return true
        } catch (error) {
            console.error('Failed to initialize E2EE system:', error)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * Generate new key pair and enable E2EE
     * @returns {Promise<boolean>} - Success status
     */
    async function enableE2EE() {
        try {
            isLoading.value = true

            // Generate new key pair
            const keyPair = await e2eeManager.generateKeyPair()
            publicKey.value = keyPair.publicKey
            keysReady.value = true // Trigger reactivity

            // Save keys to local storage
            await e2eeManager.saveKeysToStorage()

            // Update server settings
            await e2eeApiService.updateE2EESettings(publicKey.value, true)

            // Update local state
            isEnabled.value = true
            localStorage.setItem('e2ee_enabled', 'true')

            ElMessage.success('端到端加密已启用')
            return true
        } catch (error) {
            console.error('Failed to enable E2EE:', error)
            ElMessage.error('启用E2EE失败: ' + error.message)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * Disable E2EE and clear keys
     * @returns {Promise<boolean>} - Success status
     */
    async function disableE2EE() {
        try {
            isLoading.value = true

            // Update server settings
            await e2eeApiService.disableE2EE()

            // Clear all keys and data
            e2eeManager.clearKeys()
            publicKey.value = null
            contactPublicKeys.value.clear()
            encryptionStatus.value.clear()
            keysReady.value = false // Trigger reactivity

            // Clear local storage
            localStorage.setItem('e2ee_enabled', 'false')

            isEnabled.value = false

            ElMessage.success('端到端加密已禁用')
            return true
        } catch (error) {
            console.error('Failed to disable E2EE:', error)
            ElMessage.error('禁用E2EE失败: ' + error.message)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * Get public key for a contact
     * @param {number} userId - User ID
     * @returns {Promise<string|null>} - Public key or null
     */
    async function getContactPublicKey(userId) {
        try {
            // Check if we already have it cached
            if (contactPublicKeys.value.has(userId)) {
                return contactPublicKeys.value.get(userId)
            }

            // Fetch from server
            const publicKeyBase64 = await e2eeApiService.getUserPublicKey(userId)
            
            if (publicKeyBase64) {
                // Store in encryption manager and cache
                await e2eeManager.storeContactPublicKey(userId, publicKeyBase64)
                contactPublicKeys.value.set(userId, publicKeyBase64)
                return publicKeyBase64
            }

            return null
        } catch (error) {
            console.error(`Failed to get public key for user ${userId}:`, error)
            return null
        }
    }

    /**
     * Check if encryption is possible with a contact
     * @param {number} userId - User ID
     * @returns {Promise<boolean>} - Whether encryption is possible
     */
    async function canEncryptWith(userId) {
        if (!canEncrypt.value) return false

        try {
            const status = await e2eeApiService.checkE2EEStatus(
                parseInt(sessionStorage.getItem('userId') || Cookies.get('userId')),
                userId
            )
            return status.canEncrypt || false
        } catch (error) {
            console.error(`Failed to check encryption status with user ${userId}:`, error)
            return false
        }
    }

    /**
     * Encrypt a message for a contact
     * @param {string} message - Plain text message
     * @param {number} receiverId - Receiver's user ID
     * @returns {Promise<Object|null>} - Encrypted message data or null
     */
    async function encryptMessage(message, receiverId) {
        if (!canEncrypt.value) {
            throw new Error('E2EE not available')
        }

        try {
            // Ensure we have the receiver's public key
            await getContactPublicKey(receiverId)

            // Encrypt the message
            const encryptedData = await e2eeManager.encryptMessage(message, receiverId)
            
            return encryptedData
        } catch (error) {
            console.error('Failed to encrypt message:', error)
            throw error
        }
    }

    /**
     * Decrypt a received message
     * @param {string} encryptedMessage - Encrypted message content
     * @param {string} encryptedAESKey - Encrypted AES key
     * @param {string} iv - Initialization vector
     * @returns {Promise<string>} - Decrypted message
     */
    async function decryptMessage(encryptedMessage, encryptedAESKey, iv) {
        if (!isAvailable.value) {
            throw new Error('E2EE keys not available')
        }

        try {
            const decryptedMessage = await e2eeManager.decryptMessage(
                encryptedMessage,
                encryptedAESKey,
                iv
            )
            
            return decryptedMessage
        } catch (error) {
            console.error('Failed to decrypt message:', error.message)
            throw error
        }
    }

    /**
     * Send encrypted message through API
     * @param {number} receiverId - Receiver's user ID
     * @param {string} originalMessage - Original plain text message
     * @returns {Promise<Object>} - Message response in format { success: boolean, message: Object, error?: string }
     */
    async function sendEncryptedMessage(receiverId, originalMessage) {
        try {
            // Encrypt the message
            const encryptedData = await encryptMessage(originalMessage, receiverId)

            // Send through API
            const response = await e2eeApiService.sendEncryptedMessage(
                receiverId,
                encryptedData.encryptedMessage,
                encryptedData.encryptedAESKey,
                encryptedData.iv
            )

            // Store original message locally
            if (response.id) {
                localMessageStorage.storeOriginalMessage(
                    response.id.toString(),
                    originalMessage,
                    receiverId
                )
            }

            return {
                success: true,
                message: response
            }
        } catch (error) {
            console.error('Failed to send encrypted message:', error)
            return {
                success: false,
                error: error.message || 'Failed to send encrypted message'
            }
        }
    }

    /**
     * Get original message content from local storage
     * @param {string} messageId - Message ID
     * @returns {string|null} - Original message content
     */
    function getOriginalMessage(messageId) {
        return localMessageStorage.getOriginalMessage(messageId)
    }

    /**
     * Get encryption statistics
     * @returns {Object} - E2EE statistics
     */
    function getStats() {
        const storageStats = localMessageStorage.getStorageStats()
        
        return {
            enabled: isEnabled.value,
            available: isAvailable.value,
            initialized: isInitialized.value,
            hasPublicKey: !!publicKey.value,
            contactKeysCount: contactPublicKeys.value.size,
            ...storageStats
        }
    }

    /**
     * Clear all E2EE data
     */
    function clearAllData() {
        e2eeManager.clearKeys()
        localMessageStorage.clearAllMessages()
        publicKey.value = null
        contactPublicKeys.value.clear()
        encryptionStatus.value.clear()
        isEnabled.value = false
        isInitialized.value = false
    }

    /**
     * Update encryption status for a conversation
     * @param {number} userId - User ID
     * @param {boolean} status - Encryption status
     */
    function setEncryptionStatus(userId, status) {
        encryptionStatus.value.set(userId, status)
        e2eeStatuses.value.set(userId, status)
    }

    /**
     * Get encryption status for a conversation
     * @param {number} userId - User ID
     * @returns {boolean} - Encryption status
     */
    function getEncryptionStatus(userId) {
        return encryptionStatus.value.get(userId) || false
    }

    /**
     * Toggle encryption for a conversation
     * @param {number} userId - User ID
     * @returns {Promise<boolean>} - New encryption status
     */
    async function toggleEncryptionForUser(userId) {
        if (!canEncrypt.value) {
            ElMessage.warning('请先启用E2EE功能')
            return false
        }

        // Allow enabling E2EE even if the friend doesn't have keys (one-way encryption)
        // const canEncryptWithUser = await canEncryptWith(userId)
        // if (!canEncryptWithUser) {
        //     ElMessage.warning('对方未启用E2EE，无法加密聊天')
        //     return false
        // }

        const currentStatus = getEncryptionStatus(userId)
        const newStatus = !currentStatus
        setEncryptionStatus(userId, newStatus)

        ElMessage.success(newStatus ? '已启用加密聊天' : '已关闭加密聊天')
        return newStatus
    }

    /**
     * Check if E2EE is enabled between two users
     * @param {number} user1Id - First user ID
     * @param {number} user2Id - Second user ID
     * @returns {Promise<boolean>} - Whether E2EE is enabled between users
     */
    async function checkE2EEStatus(user1Id, user2Id) {
        try {
            const status = await e2eeApiService.checkE2EEStatus(user1Id, user2Id)
            return status.canEncrypt || false
        } catch (error) {
            console.error(`Failed to check E2EE status between users ${user1Id} and ${user2Id}:`, error)
            return false
        }
    }

    /**
     * Check if E2EE is enabled with a specific user
     * @param {number} userId - User ID to check
     * @returns {boolean} - Whether E2EE is enabled with this user
     */
    function isE2EEEnabledWith(userId) {
        return getEncryptionStatus(userId)
    }

    /**
     * Get friend's public key (alias for getContactPublicKey)
     * @param {number} userId - User ID
     * @returns {Promise<string|null>} - Base64 encoded public key
     */
    async function getFriendPublicKey(userId) {
        return await getContactPublicKey(userId)
    }

    /**
     * Set E2EE enabled status with a specific user
     * @param {number} userId - User ID
     * @param {boolean} enabled - Whether to enable E2EE
     * @returns {Promise<boolean>} - Success status
     */
    async function setE2EEEnabled(userId, enabled) {
        try {
            if (!isEnabled.value) {
                throw new Error('请先在设置中启用端到端加密')
            }

            // Allow enabling E2EE even if the friend doesn't have keys (one-way encryption)
            // const canEncryptWithUser = await canEncryptWith(userId)
            // if (!canEncryptWithUser && enabled) {
            //     throw new Error('对方未启用E2EE，无法开启加密聊天')
            // }

            setEncryptionStatus(userId, enabled)
            return true
        } catch (error) {
            console.error(`Failed to set E2EE status with user ${userId}:`, error)
            throw error
        }
    }

    /**
     * Store original message locally before encryption
     * @param {string} messageId - Unique message ID
     * @param {number} receiverId - Receiver's user ID
     * @param {string} originalContent - Original message content
     */
    function storeOriginalMessage(messageId, receiverId, originalContent) {
        try {
            localMessageStorage.storeOriginalMessage(messageId, originalContent, receiverId)
        } catch (error) {
            console.error('Failed to store original message:', error)
        }
    }

    /**
     * Get current user ID from session/cookies
     * @returns {number} - Current user ID
     */
    const currentUserId = computed(() => {
        return parseInt(sessionStorage.getItem('userId') || Cookies.get('userId'))
    })

    /**
     * Check if user has generated keys
     * @returns {boolean} - Whether user has keys
     */
    const hasKeys = computed(() => {
        return !!publicKey.value
    })

    /**
     * Reinitialize E2EE system (useful after login)
     * Forces reinitialization even if already initialized
     * @returns {Promise<boolean>} - Success status
     */
    async function reinitialize() {
        isInitialized.value = false
        keysReady.value = false
        return await initialize()
    }

    return {
        // State
        isEnabled,
        isInitialized,
        isLoading,
        publicKey,
        contactPublicKeys,
        encryptionStatus,
        e2eeStatuses,

        // Computed
        isAvailable,
        canEncrypt,
        currentUserId,
        hasKeys,

        // Actions
        initialize,
        reinitialize,
        enableE2EE,
        disableE2EE,
        getContactPublicKey,
        getFriendPublicKey,
        canEncryptWith,
        encryptMessage,
        decryptMessage,
        sendEncryptedMessage,
        storeOriginalMessage,
        getOriginalMessage,
        getStats,
        clearAllData,
        setEncryptionStatus,
        getEncryptionStatus,
        toggleEncryptionForUser,
        checkE2EEStatus,
        isE2EEEnabledWith,
        setE2EEEnabled
    }
})
