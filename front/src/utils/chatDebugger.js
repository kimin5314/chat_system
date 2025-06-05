import { useChatStore } from '@/store/chat'
import chatWebSocketService, { WS_MESSAGE_TYPES } from '@/services/chatWebSocket'

// Debug utilities for chat system
class ChatDebugger {
    constructor() {
        this.chatStore = useChatStore()
        this.logs = []
    }

    // Log message with timestamp
    log(message, data = null) {
        const timestamp = new Date().toISOString()
        const logEntry = { timestamp, message, data }
        this.logs.push(logEntry)
        console.log(`[Chat Debug ${timestamp}] ${message}`, data || '')
    }

    // Test WebSocket connection
    async testConnection() {
        this.log('Testing WebSocket connection...')
        try {
            const connected = await this.chatStore.connectWebSocket()
            this.log('WebSocket connection result:', connected)
            return connected
        } catch (error) {
            this.log('WebSocket connection failed:', error)
            return false
        }
    }

    // Test sending a message
    async testSendMessage(receiverId, content = 'Test message') {
        this.log('Testing send message...', { receiverId, content })
        try {
            const result = await this.chatStore.sendMessage(receiverId, content)
            this.log('Send message result:', result)
            return result
        } catch (error) {
            this.log('Send message failed:', error)
            return null
        }
    }

    // Test loading conversations
    async testLoadConversations() {
        this.log('Testing load conversations...')
        try {
            await this.chatStore.getConversations()
            this.log('Conversations loaded:', this.chatStore.conversations.length)
            return this.chatStore.conversations
        } catch (error) {
            this.log('Load conversations failed:', error)
            return []
        }
    }

    // Monitor WebSocket messages
    monitorWebSocket() {
        this.log('Starting WebSocket message monitoring...')
        
        Object.values(WS_MESSAGE_TYPES).forEach(messageType => {
            chatWebSocketService.on(messageType, (data) => {
                this.log(`WebSocket message received: ${messageType}`, data)
            })
        })
    }

    // Get connection status
    getConnectionStatus() {
        const status = {
            isConnected: this.chatStore.isConnected,
            wsState: chatWebSocketService.getConnectionState(),
            conversations: this.chatStore.conversations.length,
            messages: this.chatStore.messages.length,
            currentConversation: this.chatStore.currentConversation?.friendUsername || 'None'
        }
        this.log('Current status:', status)
        return status
    }

    // Get recent logs
    getRecentLogs(count = 10) {
        return this.logs.slice(-count)
    }

    // Clear logs
    clearLogs() {
        this.logs = []
        this.log('Debug logs cleared')
    }

    // Run comprehensive test
    async runTests(testReceiverId = null) {
        this.log('Starting comprehensive chat system tests...')
        
        const results = {
            connection: false,
            conversations: false,
            sendMessage: false,
            webSocketMonitoring: false
        }

        // Test connection
        results.connection = await this.testConnection()
        
        // Test conversations
        try {
            await this.testLoadConversations()
            results.conversations = true
        } catch (error) {
            this.log('Conversations test failed:', error)
        }

        // Test send message (if receiver ID provided)
        if (testReceiverId) {
            try {
                const message = await this.testSendMessage(testReceiverId)
                results.sendMessage = !!message
            } catch (error) {
                this.log('Send message test failed:', error)
            }
        }

        // Start monitoring
        this.monitorWebSocket()
        results.webSocketMonitoring = true

        this.log('Test results:', results)
        return results
    }
}

// Create global debug instance
const chatDebugger = new ChatDebugger()

// Make it available globally for browser console debugging
if (typeof window !== 'undefined') {
    window.chatDebugger = chatDebugger
}

export default chatDebugger
