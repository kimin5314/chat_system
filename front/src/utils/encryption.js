/**
 * End-to-End Encryption Utilities using Web Crypto API
 * Implements RSA-OAEP for key exchange and AES-GCM for message encryption
 */

class E2EEManager {
    constructor() {
        this.keyPair = null
        this.privateKey = null
        this.publicKey = null
        this.contactPublicKeys = new Map() // Store public keys of contacts
    }

    /**
     * Generate RSA key pair for E2EE
     * @returns {Promise<{publicKey: string, privateKey: CryptoKey}>}
     */
    async generateKeyPair() {
        try {
            // Generate RSA-OAEP key pair
            const keyPair = await crypto.subtle.generateKey(
                {
                    name: 'RSA-OAEP',
                    modulusLength: 2048,
                    publicExponent: new Uint8Array([1, 0, 1]),
                    hash: 'SHA-256'
                },
                true, // extractable
                ['encrypt', 'decrypt']
            )

            this.keyPair = keyPair
            this.privateKey = keyPair.privateKey
            this.publicKey = keyPair.publicKey

            // Export public key as base64 string for transmission
            const exportedPublicKey = await crypto.subtle.exportKey('spki', keyPair.publicKey)
            const publicKeyBase64 = this.arrayBufferToBase64(exportedPublicKey)

            return {
                publicKey: publicKeyBase64,
                privateKey: keyPair.privateKey
            }
        } catch (error) {
            console.error('Failed to generate key pair:', error)
            throw new Error('密钥生成失败')
        }
    }

    /**
     * Import a public key from base64 string
     * @param {string} publicKeyBase64 - Base64 encoded public key
     * @returns {Promise<CryptoKey>}
     */
    async importPublicKey(publicKeyBase64) {
        try {
            const publicKeyBuffer = this.base64ToArrayBuffer(publicKeyBase64)
            const publicKey = await crypto.subtle.importKey(
                'spki',
                publicKeyBuffer,
                {
                    name: 'RSA-OAEP',
                    hash: 'SHA-256'
                },
                false,
                ['encrypt']
            )
            return publicKey
        } catch (error) {
            console.error('Failed to import public key:', error)
            throw new Error('公钥导入失败')
        }
    }

    /**
     * Store a contact's public key
     * @param {number} userId - User ID
     * @param {string} publicKeyBase64 - Base64 encoded public key
     */
    async storeContactPublicKey(userId, publicKeyBase64) {
        try {
            const publicKey = await this.importPublicKey(publicKeyBase64)
            this.contactPublicKeys.set(userId, publicKey)
            console.log(`Stored public key for user ${userId}`)
        } catch (error) {
            console.error(`Failed to store public key for user ${userId}:`, error)
            throw error
        }
    }

    /**
     * Generate a random AES key for message encryption
     * @returns {Promise<CryptoKey>}
     */
    async generateAESKey() {
        return await crypto.subtle.generateKey(
            {
                name: 'AES-GCM',
                length: 256
            },
            true,
            ['encrypt', 'decrypt']
        )
    }

    /**
     * Encrypt a message using hybrid encryption (RSA + AES)
     * @param {string} message - Plain text message
     * @param {number} receiverId - Receiver's user ID
     * @returns {Promise<{encryptedMessage: string, encryptedAESKey: string, iv: string}>}
     */
    async encryptMessage(message, receiverId) {
        console.log('🔒 ENCRYPTION DEBUG START:', {
            messageLength: message.length,
            receiverId,
            hasContactKey: this.contactPublicKeys.has(receiverId),
            messagePreview: message.substring(0, 50) + (message.length > 50 ? '...' : '')
        })

        try {
            const receiverPublicKey = this.contactPublicKeys.get(receiverId)
            if (!receiverPublicKey) {
                throw new Error('接收者公钥未找到')
            }

            // Generate random AES key
            const aesKey = await this.generateAESKey()

            // Generate random IV for AES-GCM
            const iv = crypto.getRandomValues(new Uint8Array(12))

            // Encrypt message with AES-GCM
            const encodedMessage = new TextEncoder().encode(message)
            
            const encryptedMessage = await crypto.subtle.encrypt(
                {
                    name: 'AES-GCM',
                    iv: iv
                },
                aesKey,
                encodedMessage
            )

            // Export AES key and encrypt it with receiver's RSA public key
            const exportedAESKey = await crypto.subtle.exportKey('raw', aesKey)
            
            const encryptedAESKey = await crypto.subtle.encrypt(
                {
                    name: 'RSA-OAEP'
                },
                receiverPublicKey,
                exportedAESKey
            )

            const result = {
                encryptedMessage: this.arrayBufferToBase64(encryptedMessage),
                encryptedAESKey: this.arrayBufferToBase64(encryptedAESKey),
                iv: this.arrayBufferToBase64(iv)
            }

            console.log('🎉 ENCRYPTION SUCCESS:', {
                encryptedMessageLength: result.encryptedMessage.length,
                encryptedAESKeyLength: result.encryptedAESKey.length,
                ivLength: result.iv.length,
                encryptedMessagePreview: result.encryptedMessage.substring(0, 50) + '...',
                encryptedAESKeyPreview: result.encryptedAESKey.substring(0, 50) + '...',
                iv: result.iv
            })

            return result
        } catch (error) {
            console.error('❌ ENCRYPTION FAILED:', {
                error: error.message,
                stack: error.stack,
                receiverId,
                hasContactKey: this.contactPublicKeys.has(receiverId),
                messageLength: message.length
            })
            throw new Error(`消息加密失败: ${error.message}`)
        }
    }

    /**
     * Decrypt a message using hybrid decryption (RSA + AES)
     * @param {string} encryptedMessage - Base64 encrypted message
     * @param {string} encryptedAESKey - Base64 encrypted AES key
     * @param {string} iv - Base64 initialization vector
     * @returns {Promise<string>} - Decrypted plain text
     */
    async decryptMessage(encryptedMessage, encryptedAESKey, iv) {
        console.log('🔓 DECRYPTION DEBUG START:', {
            hasPrivateKey: !!this.privateKey,
            encryptedMessageLength: encryptedMessage?.length,
            encryptedAESKeyLength: encryptedAESKey?.length,
            ivLength: iv?.length,
            encryptedMessage: encryptedMessage?.substring(0, 50) + '...',
            encryptedAESKey: encryptedAESKey?.substring(0, 50) + '...',
            iv: iv
        })

        try {
            if (!this.privateKey) {
                throw new Error('私钥未找到')
            }

            if (!encryptedMessage || !encryptedAESKey || !iv) {
                console.error('❌ Missing decryption parameters:', {
                    hasEncryptedMessage: !!encryptedMessage,
                    hasEncryptedAESKey: !!encryptedAESKey,
                    hasIV: !!iv
                })
                throw new Error('缺少解密参数')
            }

            // Decrypt AES key with our RSA private key
            const encryptedAESKeyBuffer = this.base64ToArrayBuffer(encryptedAESKey)
            
            const decryptedAESKeyBuffer = await crypto.subtle.decrypt(
                {
                    name: 'RSA-OAEP'
                },
                this.privateKey,
                encryptedAESKeyBuffer
            )

            // Import the decrypted AES key
            const aesKey = await crypto.subtle.importKey(
                'raw',
                decryptedAESKeyBuffer,
                'AES-GCM',
                false,
                ['decrypt']
            )

            // Decrypt the message with AES-GCM
            const encryptedMessageBuffer = this.base64ToArrayBuffer(encryptedMessage)
            const ivBuffer = this.base64ToArrayBuffer(iv)
            
            console.log('🔓 Message decryption parameters:', {
                encryptedMessageBufferSize: encryptedMessageBuffer.byteLength,
                ivBufferSize: ivBuffer.byteLength
            })

            const decryptedMessageBuffer = await crypto.subtle.decrypt(
                {
                    name: 'AES-GCM',
                    iv: ivBuffer
                },
                aesKey,
                encryptedMessageBuffer
            )

            // Convert decrypted buffer back to string
            const decryptedMessage = new TextDecoder().decode(decryptedMessageBuffer)
            console.log('🎉 DECRYPTION SUCCESS:', {
                originalLength: encryptedMessage.length,
                decryptedLength: decryptedMessage.length,
                decryptedPreview: decryptedMessage.substring(0, 50) + (decryptedMessage.length > 50 ? '...' : '')
            })
            
            return decryptedMessage
        } catch (error) {
            console.error('❌ DECRYPTION FAILED:', {
                error: error.message,
                stack: error.stack,
                errorName: error.name,
                hasPrivateKey: !!this.privateKey,
                parameters: {
                    encryptedMessage: encryptedMessage?.substring(0, 50),
                    encryptedAESKey: encryptedAESKey?.substring(0, 50),
                    iv: iv
                }
            })
            throw new Error(`消息解密失败: ${error.message}`)
        }
    }

    /**
     * Load keys from local storage
     * @returns {Promise<boolean>} - Success status
     */
    async loadKeysFromStorage() {
        try {
            const privateKeyData = localStorage.getItem('e2ee_private_key')
            const publicKeyData = localStorage.getItem('e2ee_public_key')

            console.log('🔑 Storage data check:', {
                hasPrivateKeyData: !!privateKeyData,
                hasPublicKeyData: !!publicKeyData,
                privateKeyLength: privateKeyData?.length,
                publicKeyLength: publicKeyData?.length
            })

            if (privateKeyData && publicKeyData) {
                // Import private key
                const privateKeyBuffer = this.base64ToArrayBuffer(privateKeyData)
                this.privateKey = await crypto.subtle.importKey(
                    'pkcs8',
                    privateKeyBuffer,
                    {
                        name: 'RSA-OAEP',
                        hash: 'SHA-256'
                    },
                    false,
                    ['decrypt']
                )

                // Import public key
                const publicKeyBuffer = this.base64ToArrayBuffer(publicKeyData)
                this.publicKey = await crypto.subtle.importKey(
                    'spki',
                    publicKeyBuffer,
                    {
                        name: 'RSA-OAEP',
                        hash: 'SHA-256'
                    },
                    true, // Make extractable so we can export it later
                    ['encrypt']
                )

                console.log('🔑 Final key status:', {
                    privateKeyExists: !!this.privateKey,
                    publicKeyExists: !!this.publicKey,
                    isAvailable: this.isAvailable()
                })
                return true
            }
            console.log('❌ No key data found in storage')
            return false
        } catch (error) {
            console.error('❌ Failed to load keys from storage:', error)
            return false
        }
    }

    /**
     * Save keys to local storage
     * @returns {Promise<void>}
     */
    async saveKeysToStorage() {
        try {
            if (!this.privateKey || !this.publicKey) {
                throw new Error('Keys not generated')
            }

            // Export and save private key
            const exportedPrivateKey = await crypto.subtle.exportKey('pkcs8', this.privateKey)
            const privateKeyBase64 = this.arrayBufferToBase64(exportedPrivateKey)
            localStorage.setItem('e2ee_private_key', privateKeyBase64)

            // Export and save public key
            const exportedPublicKey = await crypto.subtle.exportKey('spki', this.publicKey)
            const publicKeyBase64 = this.arrayBufferToBase64(exportedPublicKey)
            localStorage.setItem('e2ee_public_key', publicKeyBase64)

            console.log('E2EE keys saved to storage')
        } catch (error) {
            console.error('Failed to save keys to storage:', error)
            throw new Error('密钥保存失败')
        }
    }

    /**
     * Get public key as base64 string
     * @returns {Promise<string>}
     */
    async getPublicKeyBase64() {
        try {
            if (!this.publicKey) {
                throw new Error('Public key not available')
            }
            const exportedPublicKey = await crypto.subtle.exportKey('spki', this.publicKey)
            return this.arrayBufferToBase64(exportedPublicKey)
        } catch (error) {
            console.error('Failed to export public key:', error)
            throw new Error('公钥导出失败')
        }
    }

    /**
     * Clear all keys and storage
     */
    clearKeys() {
        this.keyPair = null
        this.privateKey = null
        this.publicKey = null
        this.contactPublicKeys.clear()
        localStorage.removeItem('e2ee_private_key')
        localStorage.removeItem('e2ee_public_key')
        localStorage.removeItem('e2ee_enabled')
        console.log('E2EE keys cleared')
    }

    /**
     * Check if E2EE is available (keys are generated)
     * @returns {boolean}
     */
    isAvailable() {
        const available = !!(this.privateKey && this.publicKey)
        console.log('🔍 E2EE isAvailable() check:', {
            available,
            hasPrivateKey: !!this.privateKey,
            hasPublicKey: !!this.publicKey,
            privateKeyType: this.privateKey?.constructor?.name,
            publicKeyType: this.publicKey?.constructor?.name
        })
        return available
    }

    /**
     * Utility: Convert ArrayBuffer to Base64
     * @param {ArrayBuffer} buffer
     * @returns {string}
     */
    arrayBufferToBase64(buffer) {
        const bytes = new Uint8Array(buffer)
        let binary = ''
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i])
        }
        return btoa(binary)
    }

    /**
     * Utility: Convert Base64 to ArrayBuffer
     * @param {string} base64
     * @returns {ArrayBuffer}
     */
    base64ToArrayBuffer(base64) {
        const binary = atob(base64)
        const bytes = new Uint8Array(binary.length)
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i)
        }
        return bytes.buffer
    }
}

// Create singleton instance
const e2eeManager = new E2EEManager()

export default e2eeManager
export { E2EEManager }
