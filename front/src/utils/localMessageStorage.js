/**
 * Local Storage Manager for E2EE original messages
 * Stores original message content locally in case users forget what they sent
 */

class LocalMessageStorage {
    constructor() {
        this.storageKey = 'e2ee_original_messages'
        this.maxStorageSize = 1000 // Maximum number of messages to store
        this.storageTimeLimit = 30 * 24 * 60 * 60 * 1000 // 30 days in milliseconds
    }

    /**
     * Store original message content locally
     * @param {string} messageId - Unique message ID
     * @param {string} content - Original message content
     * @param {number} receiverId - Receiver's user ID
     * @param {Date} timestamp - Message timestamp
     */
    storeOriginalMessage(messageId, content, receiverId, timestamp = new Date()) {
        try {
            const messages = this.getStoredMessages()
            
            // Create message record
            const messageRecord = {
                id: messageId,
                content: content,
                receiverId: receiverId,
                timestamp: timestamp.getTime(),
                stored: Date.now()
            }

            // Add to messages
            messages[messageId] = messageRecord

            // Clean up old messages to maintain storage limit
            this.cleanupMessages(messages)

            // Save back to localStorage
            localStorage.setItem(this.storageKey, JSON.stringify(messages))
            
            console.log(`Stored original message ${messageId} locally`)
        } catch (error) {
            console.error('Failed to store original message:', error)
        }
    }

    /**
     * Retrieve original message content
     * @param {string} messageId - Message ID
     * @returns {string|null} - Original content or null if not found
     */
    getOriginalMessage(messageId) {
        try {
            const messages = this.getStoredMessages()
            const message = messages[messageId]
            
            if (message) {
                // Check if message is still within time limit
                const now = Date.now()
                if (now - message.stored <= this.storageTimeLimit) {
                    return message.content
                } else {
                    // Message expired, remove it
                    delete messages[messageId]
                    localStorage.setItem(this.storageKey, JSON.stringify(messages))
                    console.log(`Expired message ${messageId} removed from storage`)
                }
            }
            
            return null
        } catch (error) {
            console.error('Failed to retrieve original message:', error)
            return null
        }
    }

    /**
     * Get all stored messages for a specific receiver
     * @param {number} receiverId - Receiver's user ID
     * @returns {Array} - Array of message records
     */
    getMessagesForReceiver(receiverId) {
        try {
            const messages = this.getStoredMessages()
            const receiverMessages = []

            for (const messageId in messages) {
                const message = messages[messageId]
                if (message.receiverId === receiverId) {
                    // Check expiration
                    const now = Date.now()
                    if (now - message.stored <= this.storageTimeLimit) {
                        receiverMessages.push({
                            id: messageId,
                            content: message.content,
                            timestamp: new Date(message.timestamp),
                            stored: new Date(message.stored)
                        })
                    }
                }
            }

            // Sort by timestamp (newest first)
            receiverMessages.sort((a, b) => b.timestamp - a.timestamp)
            
            return receiverMessages
        } catch (error) {
            console.error('Failed to get messages for receiver:', error)
            return []
        }
    }

    /**
     * Remove a specific message from storage
     * @param {string} messageId - Message ID to remove
     */
    removeMessage(messageId) {
        try {
            const messages = this.getStoredMessages()
            if (messages[messageId]) {
                delete messages[messageId]
                localStorage.setItem(this.storageKey, JSON.stringify(messages))
                console.log(`Removed message ${messageId} from storage`)
            }
        } catch (error) {
            console.error('Failed to remove message from storage:', error)
        }
    }

    /**
     * Clear all stored messages
     */
    clearAllMessages() {
        try {
            localStorage.removeItem(this.storageKey)
            console.log('Cleared all stored original messages')
        } catch (error) {
            console.error('Failed to clear stored messages:', error)
        }
    }

    /**
     * Clear messages for a specific receiver
     * @param {number} receiverId - Receiver's user ID
     */
    clearMessagesForReceiver(receiverId) {
        try {
            const messages = this.getStoredMessages()
            let removed = 0

            for (const messageId in messages) {
                if (messages[messageId].receiverId === receiverId) {
                    delete messages[messageId]
                    removed++
                }
            }

            if (removed > 0) {
                localStorage.setItem(this.storageKey, JSON.stringify(messages))
                console.log(`Cleared ${removed} messages for receiver ${receiverId}`)
            }
        } catch (error) {
            console.error('Failed to clear messages for receiver:', error)
        }
    }

    /**
     * Get storage statistics
     * @returns {Object} - Storage stats
     */
    getStorageStats() {
        try {
            const messages = this.getStoredMessages()
            const messageCount = Object.keys(messages).length
            const storageSize = new Blob([JSON.stringify(messages)]).size
            
            // Calculate oldest and newest messages
            let oldestStored = null
            let newestStored = null
            
            for (const messageId in messages) {
                const stored = messages[messageId].stored
                if (!oldestStored || stored < oldestStored) {
                    oldestStored = stored
                }
                if (!newestStored || stored > newestStored) {
                    newestStored = stored
                }
            }

            return {
                messageCount,
                storageSize,
                maxSize: this.maxStorageSize,
                oldestMessage: oldestStored ? new Date(oldestStored) : null,
                newestMessage: newestStored ? new Date(newestStored) : null,
                retentionDays: this.storageTimeLimit / (24 * 60 * 60 * 1000)
            }
        } catch (error) {
            console.error('Failed to get storage stats:', error)
            return {
                messageCount: 0,
                storageSize: 0,
                maxSize: this.maxStorageSize,
                oldestMessage: null,
                newestMessage: null,
                retentionDays: 30
            }
        }
    }

    /**
     * Get all stored messages from localStorage
     * @returns {Object} - Messages object
     * @private
     */
    getStoredMessages() {
        try {
            const stored = localStorage.getItem(this.storageKey)
            return stored ? JSON.parse(stored) : {}
        } catch (error) {
            console.error('Failed to parse stored messages:', error)
            return {}
        }
    }

    /**
     * Clean up old messages to maintain storage limits
     * @param {Object} messages - Messages object
     * @private
     */
    cleanupMessages(messages) {
        const now = Date.now()
        const messageIds = Object.keys(messages)
        
        // Remove expired messages
        let expiredCount = 0
        for (const messageId of messageIds) {
            const message = messages[messageId]
            if (now - message.stored > this.storageTimeLimit) {
                delete messages[messageId]
                expiredCount++
            }
        }
        
        if (expiredCount > 0) {
            console.log(`Cleaned up ${expiredCount} expired messages`)
        }

        // Check if we're still over the limit
        const remainingIds = Object.keys(messages)
        if (remainingIds.length > this.maxStorageSize) {
            // Sort by storage date (oldest first) and remove excess
            const sortedMessages = remainingIds.map(id => ({
                id,
                stored: messages[id].stored
            })).sort((a, b) => a.stored - b.stored)

            const toRemove = sortedMessages.slice(0, remainingIds.length - this.maxStorageSize)
            for (const { id } of toRemove) {
                delete messages[id]
            }
            
            console.log(`Cleaned up ${toRemove.length} old messages to maintain storage limit`)
        }
    }

    /**
     * Export stored messages (for backup purposes)
     * @returns {string} - JSON string of all messages
     */
    exportMessages() {
        try {
            const messages = this.getStoredMessages()
            return JSON.stringify(messages, null, 2)
        } catch (error) {
            console.error('Failed to export messages:', error)
            return '{}'
        }
    }

    /**
     * Import messages from backup
     * @param {string} messagesJson - JSON string of messages
     * @param {boolean} merge - Whether to merge with existing messages
     */
    importMessages(messagesJson, merge = false) {
        try {
            const importedMessages = JSON.parse(messagesJson)
            
            if (merge) {
                const existingMessages = this.getStoredMessages()
                const mergedMessages = { ...existingMessages, ...importedMessages }
                this.cleanupMessages(mergedMessages)
                localStorage.setItem(this.storageKey, JSON.stringify(mergedMessages))
            } else {
                this.cleanupMessages(importedMessages)
                localStorage.setItem(this.storageKey, JSON.stringify(importedMessages))
            }
            
            console.log('Successfully imported messages')
        } catch (error) {
            console.error('Failed to import messages:', error)
            throw new Error('消息导入失败')
        }
    }
}

// Create singleton instance
const localMessageStorage = new LocalMessageStorage()

export default localMessageStorage
export { LocalMessageStorage }
