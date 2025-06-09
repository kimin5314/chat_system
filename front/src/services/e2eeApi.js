/**
 * E2EE API Service
 * Handles communication with backend E2EE endpoints
 */

import request from '@/utils/request'
import Cookies from 'js-cookie'

class E2EEApiService {
    constructor() {
        this.baseUrl = '/e2ee'
    }

    /**
     * Get authorization headers
     * @returns {Object}
     */
    getHeaders() {
        const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
        return {
            'Authorization': `Bearer ${token}`
        }
    }

    /**
     * Update user's E2EE settings
     * @param {string} publicKey - Base64 encoded public key
     * @param {boolean} enabled - Whether E2EE is enabled
     * @returns {Promise<Object>}
     */
    async updateE2EESettings(publicKey, enabled) {
        try {
            const response = await request.post(`${this.baseUrl}/settings`, {
                publicKey: publicKey,
                e2eeEnabled: enabled
            }, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                return response.data.data
            } else {
                throw new Error(response.data.message || 'Failed to update E2EE settings')
            }
        } catch (error) {
            console.error('Failed to update E2EE settings:', error)
            throw new Error('E2EE设置更新失败')
        }
    }

    /**
     * Get a user's public key
     * @param {number} userId - User ID
     * @returns {Promise<string>} - Base64 encoded public key
     */
    async getUserPublicKey(userId) {
        try {
            const response = await request.get(`${this.baseUrl}/public-key/${userId}`, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                return response.data.data.publicKey
            } else {
                throw new Error(response.data.message || 'Failed to get public key')
            }
        } catch (error) {
            console.error(`Failed to get public key for user ${userId}:`, error)
            throw new Error('获取用户公钥失败')
        }
    }

    /**
     * Send encrypted message
     * @param {number} receiverId - Receiver's user ID
     * @param {string} encryptedContent - Encrypted message content
     * @param {string} encryptedAESKey - Encrypted AES key
     * @param {string} iv - Initialization vector
     * @returns {Promise<Object>} - Message response
     */
    async sendEncryptedMessage(receiverId, encryptedContent, encryptedAESKey, iv) {
        try {
            const response = await request.post(`${this.baseUrl}/send-encrypted`, {
                receiverId: receiverId,
                encryptedContent: encryptedContent,
                encryptedAESKey: encryptedAESKey,
                iv: iv
            }, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                return response.data.data
            } else {
                throw new Error(response.data.message || 'Failed to send encrypted message')
            }
        } catch (error) {
            console.error('Failed to send encrypted message:', error)
            throw new Error('加密消息发送失败')
        }
    }

    /**
     * Check E2EE status between two users
     * @param {number} user1Id - First user ID
     * @param {number} user2Id - Second user ID
     * @returns {Promise<Object>} - E2EE status info
     */
    async checkE2EEStatus(user1Id, user2Id) {
        try {
            const response = await request.get(`${this.baseUrl}/status/${user1Id}/${user2Id}`, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                return response.data.data
            } else {
                throw new Error(response.data.message || 'Failed to check E2EE status')
            }
        } catch (error) {
            console.error(`Failed to check E2EE status between ${user1Id} and ${user2Id}:`, error)
            throw new Error('E2EE状态检查失败')
        }
    }

    /**
     * Get current user's E2EE settings
     * @returns {Promise<Object>} - User's E2EE settings
     */
    async getMyE2EESettings() {
        try {
            // We can get this from the user profile endpoint
            const response = await request.post('/user/profile', {}, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                const user = response.data.data
                return {
                    publicKey: user.publicKey,
                    e2eeEnabled: user.e2eeEnabled || false
                }
            } else {
                throw new Error(response.data.message || 'Failed to get E2EE settings')
            }
        } catch (error) {
            console.error('Failed to get my E2EE settings:', error)
            throw new Error('获取E2EE设置失败')
        }
    }

    /**
     * Disable E2EE for current user
     * @returns {Promise<Object>}
     */
    async disableE2EE() {
        try {
            const response = await request.post(`${this.baseUrl}/settings`, {
                publicKey: null,
                e2eeEnabled: false
            }, {
                headers: this.getHeaders()
            })

            if (response.data.code === "200") {
                return response.data.data
            } else {
                throw new Error(response.data.message || 'Failed to disable E2EE')
            }
        } catch (error) {
            console.error('Failed to disable E2EE:', error)
            throw new Error('E2EE禁用失败')
        }
    }

    /**
     * Get E2EE statistics
     * @returns {Promise<Object>} - E2EE usage statistics
     */
    async getE2EEStats() {
        try {
            // This could be extended if backend provides stats endpoint
            const mySettings = await this.getMyE2EESettings()
            return {
                enabled: mySettings.e2eeEnabled,
                hasPublicKey: !!mySettings.publicKey,
                keyGenerated: !!mySettings.publicKey
            }
        } catch (error) {
            console.error('Failed to get E2EE stats:', error)
            return {
                enabled: false,
                hasPublicKey: false,
                keyGenerated: false
            }
        }
    }
}

// Create singleton instance
const e2eeApiService = new E2EEApiService()

export default e2eeApiService
export { E2EEApiService }
