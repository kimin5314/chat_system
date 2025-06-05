<template>
  <div class="chat-container">
    <!-- Sidebar with conversations -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>聊天</h3>
        <div class="connection-status">
          <div class="status-indicator" :class="{ 'connected': isConnected }"></div>
          <span>{{ isConnected ? '已连接' : '未连接' }}</span>
        </div>
      </div>
      
      <div class="conversations-list" v-loading="isLoading">
        <div 
          v-for="conversation in conversations" 
          :key="conversation.friendId"
          class="conversation-item"
          :class="{ 'active': currentConversation?.friendId === conversation.friendId }"
          @click="selectConversation(conversation)"
        >
          <div class="avatar">
            <img :src="getAvatarUrl(conversation.friendAvatar)" :alt="conversation.friendUsername">
            <div v-if="conversation.isOnline" class="online-indicator"></div>
          </div>
          <div class="conversation-info">
            <div class="username">{{ conversation.friendUsername }}</div>
            <div class="last-message">{{ conversation.lastMessage || '开始聊天...' }}</div>
          </div>
          <div class="conversation-meta">
            <div class="time">{{ formatTime(conversation.lastMessageTime) }}</div>
            <div v-if="conversation.unreadCount > 0" class="unread-badge">{{ conversation.unreadCount }}</div>
          </div>
        </div>
        
        <div v-if="conversations.length === 0 && !isLoading" class="empty-conversations">
          <p>暂无聊天记录</p>
          <p>点击好友开始聊天吧</p>
        </div>
      </div>
    </div>

    <!-- Chat area -->
    <div class="chat-main">
      <div v-if="!currentConversation" class="no-conversation">
        <div class="welcome-message">
          <h3>欢迎使用聊天功能</h3>
          <p>选择一个好友开始聊天</p>
        </div>
      </div>
      
      <div v-else class="conversation-area">
        <!-- Chat header -->
        <div class="chat-header">
          <div class="friend-info">
            <img :src="getAvatarUrl(currentConversation.friendAvatar)" :alt="currentConversation.friendUsername">
            <div>
              <h4>{{ currentConversation.friendUsername }}</h4>
              <span class="status" :class="{ 'online': currentConversation.isOnline }">
                {{ currentConversation.isOnline ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Messages area -->
        <div class="messages-container" ref="messagesContainer" v-loading="isLoading">
          <div v-for="message in messages" :key="message.id" class="message">
            <div class="message-wrapper" :class="{ 'own': message.senderId === currentUserId }">
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-meta">
                  <div class="message-time">{{ formatTime(message.createdAt) }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Typing indicators -->
          <div v-if="typingUsers.size > 0" class="typing-indicator">
            <span>对方正在输入...</span>
          </div>

          <div v-if="messages.length === 0 && !isLoading" class="empty-messages">
            <p>还没有聊天记录，开始对话吧！</p>
          </div>
        </div>

        <!-- Message input -->
        <div class="message-input-area">
          <div class="input-container">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @keydown.enter.exact="sendMessage"
              @keydown.shift.enter.exact.prevent="newMessage += '\n'"
              @input="handleTyping"
              :disabled="!isConnected"
            />
            <el-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!newMessage.trim() || !isConnected"
              :loading="isSending"
              class="send-button"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChatStore } from '@/store/chat'
import { useFriendStore } from '@/store/friend'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()
const friendStore = useFriendStore()

// Reactive references
const newMessage = ref('')
const messagesContainer = ref(null)
const typingTimer = ref(null)
const isSending = ref(false)

// Computed properties
const conversations = computed(() => chatStore.conversations)
const currentConversation = computed(() => chatStore.currentConversation)
const messages = computed(() => {
  // Include forceRefresh to trigger reactivity updates
  chatStore.forceRefresh
  return chatStore.messages
})
const isLoading = computed(() => chatStore.isLoading)
const isConnected = computed(() => chatStore.isConnected)
const typingUsers = computed(() => chatStore.typingUsers)

const currentUserId = computed(() => {
  return parseInt(sessionStorage.getItem('userId') || Cookies.get('userId'))
})

// Helper function to construct avatar URL
const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) return '/default-avatar.png'
  if (avatarPath.startsWith('http')) return avatarPath
  const baseUrl = import.meta.env.VITE_API_BASE
  return `${baseUrl}${avatarPath.startsWith('/') ? avatarPath : '/' + avatarPath}`
}

// Initialize chat
onMounted(async () => {
  try {
    // Initialize chat store (connects WebSocket and loads conversations)
    await chatStore.initialize()
    
    // If there's a friendId in route params, start conversation with that friend
    const friendId = route.params.friendId || route.query.friendId
    if (friendId) {
      await startConversationWithFriend(parseInt(friendId))
    }
  } catch (error) {
    console.error('Failed to initialize chat:', error)
    ElMessage.error('聊天初始化失败')
  }
})

// Cleanup on unmount
onUnmounted(() => {
  chatStore.cleanup()
})

// Watch for route changes to handle direct navigation to chat with specific friend
watch(() => route.params.friendId, async (newFriendId) => {
  if (newFriendId) {
    await startConversationWithFriend(parseInt(newFriendId))
  }
})

// Watch messages to auto-scroll
watch(messages, async () => {
  await nextTick()
  scrollToBottom()
}, { deep: true })

// Methods
async function startConversationWithFriend(friendId) {
  try {
    // Check if conversation already exists
    let existingConversation = conversations.value.find(c => c.friendId === friendId)
    
    if (existingConversation) {
      // Conversation exists, select it and load messages
      await selectConversation(existingConversation)
      return
    }

    // No conversation exists, create a new conversation interface
    // First get friend info
    await friendStore.getFriendsList()
    const friend = friendStore.friends.find(f => f.id === friendId)
    
    if (!friend) {
      ElMessage.error('好友不存在')
      return
    }

    // Create new conversation object (not persisted until first message)
    const newConversation = {
      friendId: friend.id,
      friendUsername: friend.username,
      friendAvatar: friend.avatar,
      lastMessage: '',
      unreadCount: 0,
      isOnline: friend.online || false,
      lastMessageTime: new Date(),
      isNew: true // Flag to indicate this is a new conversation
    }

    // Add to conversations list
    conversations.value.unshift(newConversation)
    chatStore.setCurrentConversation(newConversation)
    
    // Clear messages since this is a new conversation
    chatStore.messages.length = 0

    console.log('New conversation interface created for friend:', friend.username)
  } catch (error) {
    console.error('Failed to start conversation:', error)
    ElMessage.error('启动聊天失败')
  }
}

async function selectConversation(conversation) {
  try {
    chatStore.setCurrentConversation(conversation)
    
    // Load messages for this conversation
    await chatStore.getConversationMessages(conversation.friendId)
    
    // Mark messages as read
    await chatStore.markMessagesAsRead(conversation.friendId)
    
    // Reset unread count
    conversation.unreadCount = 0
    
    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Failed to select conversation:', error)
    ElMessage.error('加载聊天记录失败')
  }
}

async function sendMessage() {
  if (!newMessage.value.trim() || !currentConversation.value) return

  try {
    isSending.value = true
    const messageContent = newMessage.value.trim()
    newMessage.value = ''

    // Send regular message via chat store
    const message = await chatStore.sendMessage(
      currentConversation.value.friendId,
      messageContent
    )

    // If this was a new conversation, mark it as no longer new
    if (currentConversation.value.isNew) {
      currentConversation.value.isNew = false
      // Refresh conversations to get the real conversation from server
      await chatStore.getConversations()
    }

    // Scroll to bottom
    await nextTick()
    scrollToBottom()

    console.log('Message sent successfully:', message)
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('发送消息失败')
  } finally {
    isSending.value = false
  }
}

function handleTyping() {
  if (!currentConversation.value) return

  // Send typing indicator
  chatStore.sendTypingIndicator(currentConversation.value.friendId, true)

  // Clear previous timer
  if (typingTimer.value) {
    clearTimeout(typingTimer.value)
  }

  // Stop typing indicator after 3 seconds
  typingTimer.value = setTimeout(() => {
    chatStore.sendTypingIndicator(currentConversation.value.friendId, false)
  }, 3000)
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function formatTime(time) {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // Less than 1 minute
  if (diff < 60000) {
    return '刚刚'
  }
  
  // Less than 1 hour
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  
  // Same day
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // Yesterday
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  // Other days
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// Expose methods for external use (e.g., from FriendList component)
defineExpose({
  startConversationWithFriend
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.chat-sidebar {
  width: 320px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff4757;
}

.status-indicator.connected {
  background: #2ed573;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background: #f8f9fa;
}

.conversation-item.active {
  background: #e3f2fd;
}

.avatar {
  position: relative;
  margin-right: 12px;
}

.avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #2ed573;
  border: 2px solid white;
  border-radius: 50%;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.username {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.last-message {
  color: #666;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.time {
  font-size: 12px;
  color: #999;
}

.unread-badge {
  background: #ff4757;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.empty-conversations {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-conversation {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-message {
  text-align: center;
  color: #666;
}

.welcome-message h3 {
  margin-bottom: 8px;
  color: #333;
}

.conversation-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-info img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.friend-info h4 {
  margin: 0;
  color: #333;
}

.status {
  font-size: 12px;
  color: #999;
}

.status.online {
  color: #2ed573;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-wrapper {
  display: flex;
}

.message-wrapper.own {
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
  background: #f0f0f0;
  padding: 10px 15px;
  border-radius: 18px;
  position: relative;
}

.message-wrapper.own .message-content {
  background: #1976d2;
  color: white;
}

.message-text {
  word-break: break-word;
  line-height: 1.4;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.message-wrapper.own .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.typing-indicator {
  padding: 10px 15px;
  background: #f0f0f0;
  border-radius: 18px;
  max-width: 200px;
  color: #666;
  font-style: italic;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

.empty-messages {
  text-align: center;
  color: #666;
  padding: 40px;
}

.message-input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background: white;
}

.input-container {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-container :deep(.el-textarea) {
  flex: 1;
}

.send-button {
  padding: 8px 20px;
  height: auto;
}

/* Scrollbar styling */
.conversations-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.conversations-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Responsive design */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 100%;
    position: absolute;
    z-index: 10;
    height: 100%;
  }
  
  .chat-main {
    width: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>
