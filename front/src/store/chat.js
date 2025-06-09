import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import Cookies from 'js-cookie'
import chatWebSocketService, { WS_MESSAGE_TYPES } from '@/services/chatWebSocket'
import { useFriendStore } from '@/store/friend'
import { useE2EEStore } from '@/store/e2ee'
import e2eeManager from '@/utils/encryption'

export const useChatStore = defineStore('chat', () => {
    const conversations = ref([])
    const currentConversation = ref(null)
    const messages = ref([])
    const isLoading = ref(false)
    const isConnected = ref(false)
    const typingUsers = ref(new Set())
    const messageUpdateTrigger = ref(0) // Helper to force UI updates

    // Computed properties
    const currentUserId = computed(() => {
        return parseInt(sessionStorage.getItem('userId') || Cookies.get('userId'))
    })

    // WebSocket connection management
    async function connectWebSocket() {
        try {
            // Set loading state during connection attempt
            isLoading.value = true
            
            // Use token from cookies primarily
            const token = Cookies.get('token')
            if (!token) {
                console.warn('No token available for WebSocket connection')
                isConnected.value = false
                return false
            }

            // Store token for WebSocket reconnection
            sessionStorage.setItem('chatToken', token)

            // Connect with timeout to handle long-running connection attempts
            await Promise.race([
                chatWebSocketService.connect(token),
                new Promise((_, reject) => 
                    setTimeout(() => reject(new Error('Connection timeout')), 10000)
                )
            ])
            
            // Check if actually connected
            if (chatWebSocketService.isConnected()) {
                isConnected.value = true
                setupWebSocketHandlers()
                return true
            } else {
                isConnected.value = false
                return false
            }
        } catch (error) {
            console.error('WebSocket连接失败:', error)
            isConnected.value = false
            return false
        } finally {
            isLoading.value = false
        }
    }

    function disconnectWebSocket() {
        chatWebSocketService.disconnect()
        isConnected.value = false
    }

    // Setup WebSocket message handlers
    function setupWebSocketHandlers() {
        // Handle new messages
        chatWebSocketService.on(WS_MESSAGE_TYPES.NEW_MESSAGE, async (messageDto) => {
            await addMessage(messageDto)
        })

        // Handle encrypted messages
        chatWebSocketService.on(WS_MESSAGE_TYPES.ENCRYPTED_MESSAGE, async (messageDto) => {
            await handleEncryptedMessage(messageDto)
        })

        // Handle user online/offline status
        chatWebSocketService.on(WS_MESSAGE_TYPES.USER_ONLINE, (data) => {
            updateUserStatus(data.userId, true)
        })

        chatWebSocketService.on(WS_MESSAGE_TYPES.USER_OFFLINE, (data) => {
            updateUserStatus(data.userId, false)
        })

        // Handle friend status updates (for backwards compatibility)
        chatWebSocketService.on(WS_MESSAGE_TYPES.FRIEND_ONLINE, (data) => {
            updateUserStatus(data.userId, true)
        })

        chatWebSocketService.on(WS_MESSAGE_TYPES.FRIEND_OFFLINE, (data) => {
            updateUserStatus(data.userId, false)
        })

        // Handle message read status
        chatWebSocketService.on(WS_MESSAGE_TYPES.MESSAGE_READ, (data) => {
            markLocalMessagesAsRead(data.senderId, data.receiverId)
        })

        // Handle typing indicators
        chatWebSocketService.on(WS_MESSAGE_TYPES.TYPING, (data) => {
            handleTypingIndicator(data)
        })

        // Handle friend requests and responses
        chatWebSocketService.on(WS_MESSAGE_TYPES.FRIEND_REQUEST, (data) => {
            const friendStore = useFriendStore()
            friendStore.addFriendRequest(data)
        })

        chatWebSocketService.on(WS_MESSAGE_TYPES.FRIEND_RESPONSE, (data) => {
            if (data.accepted) {
                const friendStore = useFriendStore()
                friendStore.addFriend(data.friend)
            }
        })

        // Handle connection status
        chatWebSocketService.onConnection((connected) => {
            isConnected.value = connected
            // Don't automatically refresh conversations on reconnect to preserve virtual conversations
            if (connected) {
                // WebSocket reconnected - no console log needed
            }
        })
    }
    
    // Get all conversations
    async function getConversations() {
        try {
            isLoading.value = true
            const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
            const res = await axios.get(`${import.meta.env.VITE_API_BASE}/messages/conversations`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            
            if (res.data.code === "200") {
                conversations.value = res.data.data
            }
        } catch (err) {
            console.error('Failed to get conversations:', err)
            throw err
        } finally {
            isLoading.value = false
        }
    }
    
    // Get conversation messages
    async function getConversationMessages(friendId) {
        try {
            isLoading.value = true
            const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
            const res = await axios.get(`${import.meta.env.VITE_API_BASE}/messages/conversation/${friendId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            
            if (res.data.code === "200") {
                const rawMessages = res.data.data
                const e2eeStore = useE2EEStore()
                
                // Process messages to decrypt encrypted ones
                const processedMessages = await Promise.all(rawMessages.map(async (message) => {
                    // For all messages, first check localStorage
                    const originalContent = e2eeStore.getOriginalMessage(message.id?.toString())
                    
                    if (originalContent) {
                        return {
                            ...message,
                            content: originalContent,
                            isDecrypted: false, // It's the original, not decrypted
                            isEncrypted: message.isEncrypted || false,
                            originalContent: originalContent,
                            source: 'localStorage'
                        }
                    }
                    
                    // If not in localStorage and message is encrypted, try to decrypt
                    if (message.isEncrypted && (message.encryptedContent || message.content)) {
                        try {
                            const decryptedContent = await e2eeStore.decryptMessage(
                                message.encryptedContent || message.content,
                                message.encryptedAESKey,
                                message.iv
                            )
                            
                            return {
                                ...message,
                                content: decryptedContent,
                                isDecrypted: true,
                                isEncrypted: true,
                                originalEncrypted: message.encryptedContent || message.content,
                                source: 'decrypted'
                            }
                        } catch (err) {
                            console.error('Failed to decrypt message:', err.message)
                            // Show encrypted message with error indicator
                            return {
                                ...message,
                                content: '[无法解密的消息]',
                                isDecryptionError: true,
                                originalEncrypted: message.encryptedContent || message.content,
                                source: 'decryption_failed'
                            }
                        }
                    }
                    
                    // Return regular message as-is
                    return {
                        ...message,
                        source: 'regular'
                    }
                }))
                
                messages.value = processedMessages
                // Mark messages as read
                await markMessagesAsRead(friendId)
            }
        } catch (err) {
            console.error('Failed to get conversation messages:', err)
            throw err
        } finally {
            isLoading.value = false
        }
    }
    
    // Send message
    async function sendMessage(receiverId, content, messageType = 'TEXT') {
        try {
            const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
            const e2eeStore = useE2EEStore()
            
            // Check if E2EE is enabled locally and for this conversation
            const isLocalE2EEEnabled = e2eeStore.isEnabled && e2eeStore.hasKeys
            const isConversationEncrypted = e2eeStore.isE2EEEnabledWith(receiverId)
            
            if (isLocalE2EEEnabled && isConversationEncrypted && messageType === 'TEXT') {
                // Send encrypted message
                return await sendEncryptedMessage(receiverId, content)
            } else {
                // Send regular message
                const res = await axios.post(`${import.meta.env.VITE_API_BASE}/messages/send`, {
                    receiverId,
                    content,
                    messageType
                }, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                })
                
                if (res.data.code === "200") {
                    const messageData = res.data.data
                    
                    // Add message to current conversation if it's the active one
                    if (currentConversation.value && currentConversation.value.friendId === receiverId) {
                        messages.value.push(messageData)
                    }
                    
                    // Update conversation list locally
                    updateConversationWithMessage(messageData)
                    
                    // If this was a new conversation, refresh conversations to get real data from server
                    if (currentConversation.value && currentConversation.value.isNew) {
                        currentConversation.value.isNew = false
                        // Small delay to ensure message is processed on server
                        setTimeout(async () => {
                            await getConversations()
                        }, 500)
                    }
                    
                    return messageData
                } else {
                    console.error('Server returned error:', res.data)
                    throw new Error(res.data.message || 'Failed to send message')
                }
            }
        } catch (err) {
            console.error('Failed to send message:', err)
            throw err
        }
    }
    
    // Send encrypted message
    async function sendEncryptedMessage(receiverId, content) {
        try {
            const e2eeStore = useE2EEStore()
            
            // Encrypt and send message
            const result = await e2eeStore.sendEncryptedMessage(receiverId, content)
            
            if (result.success) {
                const messageData = result.message
                
                // Add message to current conversation if it's the active one
                // Sender should see their original message, not the encrypted version
                if (currentConversation.value && currentConversation.value.friendId === receiverId) {
                    const displayMessage = {
                        ...messageData,
                        content: content, // Show original content to sender
                        isEncrypted: true,
                        isDecrypted: false, // Sender doesn't need decryption flag
                        originalContent: content
                    }
                    messages.value.push(displayMessage)
                }
                
                // Update conversation list locally with decrypted content for display
                const displayMessage = { ...messageData, content }
                updateConversationWithMessage(displayMessage)
                
                // If this was a new conversation, refresh conversations to get real data from server
                if (currentConversation.value && currentConversation.value.isNew) {
                    currentConversation.value.isNew = false
                    setTimeout(async () => {
                        await getConversations()
                    }, 500)
                }
                
                return messageData
            } else {
                throw new Error(result.error || 'Failed to send encrypted message')
            }
        } catch (err) {
            console.error('Failed to send encrypted message:', err)
            throw err
        }
    }
    
    // Handle incoming encrypted message
    async function handleEncryptedMessage(messageDto) {
        try {
            const e2eeStore = useE2EEStore()
            
            // Decrypt the message with all required parameters
            const decryptedContent = await e2eeStore.decryptMessage(
                messageDto.content || messageDto.encryptedContent,
                messageDto.encryptedAESKey,
                messageDto.iv
            )
            
            // Create decrypted message object
            const decryptedMessage = {
                ...messageDto,
                content: decryptedContent,
                isDecrypted: true,
                isEncrypted: true,
                originalEncrypted: messageDto.content || messageDto.encryptedContent
            }
            
            // Add to current conversation
            addMessage(decryptedMessage)
            
        } catch (err) {
            console.error('Failed to decrypt message:', err)
            
            // Show encrypted message with error indicator
            const errorMessage = {
                ...messageDto,
                content: '[无法解密的消息]',
                isDecryptionError: true,
                originalEncrypted: messageDto.content || messageDto.encryptedContent
            }
            
            addMessage(errorMessage)
        }
    }
    
    // Mark messages as read
    async function markMessagesAsRead(senderId) {
        try {
            const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
            await axios.post(`${import.meta.env.VITE_API_BASE}/messages/mark-read/${senderId}`, {}, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            
            // Send WebSocket notification about read status
            if (isConnected.value) {
                // Get user ID from sessionStorage first, then fallback to cookies
                const userId = sessionStorage.getItem('userId') || Cookies.get('userId')
                chatWebSocketService.send(WS_MESSAGE_TYPES.MESSAGE_READ, {
                    senderId,
                    receiverId: userId
                })
            }
        } catch (err) {
            console.error('Failed to mark messages as read:', err)
        }
    }
    
    // Add new message from WebSocket
    async function addMessage(message) {
        // Process the message (decrypt if needed) before adding
        let processedMessage = message

        // Check if message is encrypted and needs decryption
        if (message.isEncrypted && message.encryptedAESKey && message.iv) {
            try {
                const e2eeStore = useE2EEStore()
                
                // First check if we have the original content in localStorage
                const originalContent = e2eeStore.getOriginalMessage(message.id?.toString())
                
                if (originalContent) {
                    processedMessage = {
                        ...message,
                        content: originalContent,
                        isDecrypted: false, // It's the original, not decrypted
                        originalContent: originalContent,
                        source: 'localStorage'
                    }
                } else {
                    // Try to decrypt the message
                    const decryptedContent = await e2eeStore.decryptMessage(
                        message.content, // The encrypted content is in the content field
                        message.encryptedAESKey,
                        message.iv
                    )
                    
                    processedMessage = {
                        ...message,
                        content: decryptedContent,
                        isDecrypted: true,
                        originalContent: decryptedContent
                    }
                }
            } catch (error) {
                console.error('Failed to decrypt WebSocket message:', error.message)
                // Keep the message as is, showing encrypted content
                processedMessage = {
                    ...message,
                    content: message.content, // Keep the encrypted content
                    isDecrypted: false,
                    decryptionError: error.message
                }
            }
        }

        // Add to current conversation if it's active
        if (currentConversation.value && 
            (processedMessage.senderId === currentConversation.value.friendId || 
             processedMessage.receiverId === currentConversation.value.friendId)) {
            
            // Check if message already exists to avoid duplicates
            const existingMessage = messages.value.find(m => m.id === processedMessage.id)
            if (!existingMessage) {
                messages.value.push(processedMessage)
                // Trigger reactivity update
                messageUpdateTrigger.value++
            }
        }
        
        // Update existing conversation with new message instead of refreshing from server
        updateConversationWithMessage(processedMessage)
    }
    
    // Update conversation list with new message without server call
    function updateConversationWithMessage(message) {
        const senderId = message.senderId
        const receiverId = message.receiverId
        const currentUserId = parseInt(sessionStorage.getItem('userId') || Cookies.get('userId'))
        
        // Determine which user is the "friend" in the conversation
        const friendId = senderId === currentUserId ? receiverId : senderId
        
        // Find existing conversation
        let conversation = conversations.value.find(c => c.friendId === friendId)
        
        if (conversation) {
            // Update existing conversation
            conversation.lastMessage = message.content
            conversation.lastMessageTime = message.createdAt || new Date()
            
            // Remove isNew flag if it exists
            if (conversation.isNew) {
                conversation.isNew = false
            }
            
            // Increment unread count if message is not from current user and not current conversation
            if (senderId !== currentUserId && (!currentConversation.value || currentConversation.value.friendId !== friendId)) {
                conversation.unreadCount = (conversation.unreadCount || 0) + 1
            }
            
            // Move conversation to top
            const index = conversations.value.indexOf(conversation)
            if (index > 0) {
                conversations.value.splice(index, 1)
                conversations.value.unshift(conversation)
            }
        }
        // Don't automatically create conversations from incoming messages
        // They should be created explicitly when starting conversation with a friend
    }
    
    // Set current conversation
    function setCurrentConversation(conversation) {
        currentConversation.value = conversation
        // Clear typing indicators when switching conversations
        typingUsers.value.clear()
    }
    
    // Update user online status
    function updateUserStatus(userId, isOnline) {
        // Update conversation status
        const conversation = conversations.value.find(c => c.friendId === userId)
        if (conversation) {
            conversation.isOnline = isOnline
        }
        
        // Update friend status in friend store
        const friendStore = useFriendStore()
        friendStore.updateFriendStatus(userId, isOnline)
    }
    
    // Mark local messages as read (from WebSocket)
    function markLocalMessagesAsRead(senderId, receiverId) {
        messages.value.forEach(message => {
            if (message.senderId === senderId && message.receiverId === receiverId) {
                message.isRead = true
            }
        })
        
        // Update conversation unread count
        const conversation = conversations.value.find(c => c.friendId === senderId)
        if (conversation) {
            conversation.unreadCount = 0
        }
    }
    
    // Handle typing indicator
    function handleTypingIndicator(data) {
        const { userId, isTyping } = data
        
        if (isTyping) {
            typingUsers.value.add(userId)
        } else {
            typingUsers.value.delete(userId)
        }
        
        // Auto-clear typing indicator after 3 seconds
        if (isTyping) {
            setTimeout(() => {
                typingUsers.value.delete(userId)
            }, 3000)
        }
    }
    
    // Send typing indicator
    function sendTypingIndicator(receiverId, isTyping) {
        if (isConnected.value) {
            chatWebSocketService.send(WS_MESSAGE_TYPES.TYPING, {
                receiverId,
                isTyping
            })
        }
    }
    
    // Get unread message count for a specific friend
    async function getUnreadCount(friendId) {
        try {
            const token = sessionStorage.getItem('chatToken') || Cookies.get('token')
            const res = await axios.get(`${import.meta.env.VITE_API_BASE}/messages/unread-count/${friendId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            
            if (res.data.code === "200") {
                return res.data.data
            }
        } catch (err) {
            console.error('Failed to get unread count:', err)
        }
        return 0
    }
    
    // Start conversation with friend (proper conversation handling)
    async function startConversationWithFriend(friendId) {
        try {
            // First check if conversation already exists
            const existingConversation = conversations.value.find(c => c.friendId == friendId)
            if (existingConversation) {
                setCurrentConversation(existingConversation)
                await getConversationMessages(friendId)
                return existingConversation
            }

            // Fetch friend info from friend store
            const friendStore = useFriendStore()
            await friendStore.getFriendsList()
            const friend = friendStore.friends.find(f => f.id == friendId)
            if (!friend) {
                throw new Error(`Friend with ID ${friendId} not found`) 
            }
            
            // Create conversation interface (will be persisted when first message is sent)
            const newConversation = {
                friendId: friend.id,
                friendUsername: friend.username,
                friendDisplayName: friend.displayName,
                friendAvatar: friend.avatar,
                lastMessage: '',
                unreadCount: 0,
                isOnline: friend.online || false,
                lastMessageTime: new Date(),
                isNew: true // Flag to indicate this conversation needs to be created on server
            }
            
            // Add to conversations list
            conversations.value.unshift(newConversation)
            setCurrentConversation(newConversation)
            
            // Clear messages since this is a new conversation
            messages.value = []
            
            return newConversation
        } catch (err) {
            console.error('Failed to start conversation with friend:', err)
            throw err
        }
    }

    // Initialize chat store
    async function initialize() {
        const connected = await connectWebSocket()
        await getConversations()
        
        // Return connection status
        return {
            connected: connected,
            conversations: conversations.value
        }
    }
    
    // Cleanup
    function cleanup() {
        disconnectWebSocket()
        conversations.value = []
        messages.value = []
        currentConversation.value = null
        typingUsers.value.clear()
        // Clear stored token on cleanup
        sessionStorage.removeItem('chatToken')
    }
    
    return {
        // State
        conversations,
        currentConversation,
        messages,
        isLoading,
        isConnected,
        typingUsers,
        messageUpdateTrigger,
        
        // WebSocket methods
        connectWebSocket,
        disconnectWebSocket,
        reconnectWebSocket: async () => {
            // Force a reconnection of the WebSocket
            disconnectWebSocket()
            return await connectWebSocket()
        },
        
        // API methods
        getConversations,
        getConversationMessages,
        sendMessage,
        sendEncryptedMessage,
        handleEncryptedMessage,
        markMessagesAsRead,
        getUnreadCount,
        startConversationWithFriend,
        
        // Local state methods
        addMessage,
        setCurrentConversation,
        updateUserStatus,
        markLocalMessagesAsRead,
        sendTypingIndicator,
        
        // Lifecycle methods
        initialize,
        cleanup,
        startConversationWithFriend,
    }
})
