<template>
  <div class="chat-container">
    <!-- Mobile sidebar toggle -->
    <div class="mobile-header" v-if="isMobile">
      <el-button @click="toggleSidebar" class="sidebar-toggle">
        <el-icon><Menu /></el-icon>
        聊天列表
      </el-button>
      <div v-if="currentConversation" class="mobile-current-chat">
        {{ currentConversation.friendUsername }}
      </div>
    </div>

    <!-- Mobile backdrop -->
    <div 
      v-if="isMobile && sidebarVisible" 
      class="mobile-backdrop"
      @click="toggleSidebar"
    ></div>

    <!-- Sidebar on desktop, bottom drawer on mobile -->
    <div class="chat-sidebar" :class="{
      'sidebar-hidden': isMobile && !sidebarVisible,
      'sidebar-visible': isMobile && sidebarVisible
    }">
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
import { Menu } from '@element-plus/icons-vue'
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

// Mobile responsiveness
const isMobile = ref(false)
const sidebarVisible = ref(false)

// Computed properties
const conversations = computed(() => chatStore.conversations)
const currentConversation = computed(() => chatStore.currentConversation)
const messages = computed(() => {
  // Include messageUpdateTrigger to trigger reactivity updates
  chatStore.messageUpdateTrigger
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
  if (!avatarPath) return 'https://avatars.githubusercontent.com/u/583231?v=4'
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
    
    // Close sidebar on mobile after selection
    if (isMobile.value) {
      sidebarVisible.value = false
    }
    
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

// Mobile responsiveness functions
function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    sidebarVisible.value = false // Reset sidebar visibility on desktop
  }
}

// Initialize mobile detection
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// Expose methods for external use (e.g., from FriendList component)
defineExpose({
  startConversationWithFriend
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 80px);
  background: var(--primary-gradient);
  position: relative;
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-heavy);
  animation: slideUp 0.5s ease;
  margin: var(--spacing-xl);
}

.mobile-header {
  display: none;
}

.mobile-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  z-index: 900;
  animation: fadeIn 0.3s ease;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.mobile-current-chat {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.sidebar-hidden {
  transform: translateY(100%);
}

.sidebar-visible {
  transform: translateY(0);
}

.chat-sidebar {
  width: 320px;
  background: var(--bg-primary);
  backdrop-filter: blur(10px);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-medium);
}

.sidebar-header {
  padding: var(--spacing-xl) var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-secondary);
}

.sidebar-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
}

.connection-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger-color);
  transition: background-color 0.3s ease;
}

.status-indicator.connected {
  background: var(--success-color);
  box-shadow: 0 0 6px var(--success-color-alpha);
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: var(--spacing-lg);
  cursor: pointer;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.3s ease;
  position: relative;
}

.conversation-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: var(--primary-gradient);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.conversation-item:hover {
  background: var(--bg-hover);
  transform: translateX(4px);
}

.conversation-item.active {
  background: var(--primary-color-alpha);
  border-left: 3px solid var(--primary-color);
}

.conversation-item.active::before {
  transform: scaleY(1);
}

.avatar {
  position: relative;
  margin-right: var(--spacing-md);
}

.avatar img {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--bg-primary);
  box-shadow: var(--shadow-light);
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  background: var(--success-color);
  border: 3px solid var(--bg-primary);
  border-radius: 50%;
  box-shadow: 0 2px 6px var(--success-color-alpha);
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.username {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-sm);
}

.last-message {
  color: var(--text-secondary);
  font-size: var(--font-xs);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.conversation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--spacing-xs);
}

.time {
  font-size: var(--font-xs);
  color: var(--text-muted);
}

.unread-badge {
  background: var(--danger-color);
  color: white;
  border-radius: var(--border-radius);
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--font-xs);
  min-width: 20px;
  text-align: center;
  font-weight: var(--font-semibold);
  box-shadow: var(--shadow-light);
}

.empty-conversations {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-lg);
  color: var(--text-secondary);
}

.empty-conversations p:first-child {
  font-size: var(--font-md);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.empty-conversations p:last-child {
  font-size: var(--font-sm);
  margin: 0;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
}

.no-conversation {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
}

.welcome-message {
  text-align: center;
  color: white;
  padding: var(--spacing-3xl);
  background: var(--primary-gradient);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.welcome-message h3 {
  margin-bottom: var(--spacing-md);
  color: white;
  font-size: var(--font-2xl);
  font-weight: var(--font-semibold);
}

.welcome-message p {
  font-size: var(--font-md);
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
}

.conversation-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  min-height: 0;
  height: 100%;
  position: relative;
  border-radius: 0 var(--border-radius) var(--border-radius) 0;
}

.chat-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-light);
  flex-shrink: 0;
  z-index: 10;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.friend-info img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--bg-primary);
  box-shadow: var(--shadow-light);
}

.friend-info h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
}

.status {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

.status.online {
  color: var(--success-color);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.status.online::before {
  content: '';
  width: 8px;
  height: 8px;
  background: var(--success-color);
  border-radius: 50%;
  box-shadow: 0 0 6px var(--success-color-alpha);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  min-height: 0;
}

.message-wrapper {
  display: flex;
}

.message-wrapper.own {
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius);
  position: relative;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
}

.message-wrapper.own .message-content {
  background: var(--primary-gradient);
  color: white;
  box-shadow: var(--shadow-medium);
}

.message-text {
  word-break: break-word;
  line-height: 1.5;
  font-size: var(--font-sm);
}

.message-time {
  font-size: var(--font-xs);
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
  text-align: right;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-xs);
}

.message-wrapper.own .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.typing-indicator {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  border-radius: var(--border-radius);
  max-width: 200px;
  color: var(--text-secondary);
  font-style: italic;
  font-size: var(--font-xs);
  animation: pulse 1.5s infinite;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
}

.empty-messages {
  text-align: center;
  color: var(--text-secondary);
  padding: var(--spacing-3xl) var(--spacing-lg);
  font-size: var(--font-sm);
  background: var(--bg-hover);
  border-radius: var(--border-radius);
  margin: var(--spacing-2xl) 0;
  border: 1px dashed var(--border-light);
}

.message-input-area {
  position: sticky;
  bottom: 0;
  padding: var(--spacing-lg);
  border-top: 1px solid var(--border-light);
  background: var(--bg-primary);
  flex-shrink: 0;
  z-index: 2;
}

.input-container {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-end;
  background: var(--bg-secondary);
  padding: var(--spacing-md);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-light);
}

.input-container :deep(.el-textarea) {
  flex: 1;
}

.input-container :deep(.el-textarea .el-textarea__inner) {
  border: none;
  background: transparent;
  box-shadow: none;
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-sm);
  line-height: 1.5;
  resize: none;
  color: var(--text-primary);
}

.input-container :deep(.el-textarea .el-textarea__inner):focus {
  border: none;
  box-shadow: none;
}

.send-button {
  padding: var(--spacing-sm) var(--spacing-lg);
  height: auto;
  background: var(--primary-gradient);
  border: none;
  border-radius: var(--border-radius-small);
  font-weight: var(--font-semibold);
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  color: white;
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.send-button:active {
  transform: translateY(0);
}

/* Custom scrollbar for messages */
.messages-container::-webkit-scrollbar {
  width: 10px;
}

.messages-container::-webkit-scrollbar-track {
  background: var(--bg-hover);
  border-radius: var(--border-radius-small);
  margin: var(--spacing-sm) 0;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--primary-color-alpha);
  border-radius: var(--border-radius-small);
  border: 2px solid var(--bg-hover);
  transition: all 0.3s ease;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--primary-color);
  border: 1px solid var(--bg-hover);
}

/* Conversations list scrollbar */
.conversations-list::-webkit-scrollbar {
  width: 8px;
}

.conversations-list::-webkit-scrollbar-track {
  background: var(--bg-hover);
  border-radius: var(--border-radius-small);
}

.conversations-list::-webkit-scrollbar-thumb {
  background: var(--primary-color-alpha);
  border-radius: var(--border-radius-small);
  transition: background 0.3s ease;
}

.conversations-list::-webkit-scrollbar-thumb:hover {
  background: var(--primary-color);
}

/* Mobile responsiveness */
@media (max-width: 1024px) {
  .chat-sidebar {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .chat-container {
    border-radius: var(--border-radius-small);
    margin: var(--spacing-md);
    height: calc(100vh - 60px);
    flex-direction: column;
  }
  
  .mobile-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-md);
    background: var(--bg-primary);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--border-light);
    position: sticky;
    top: 0;
    z-index: 902;
    box-shadow: var(--shadow-light);
  }
  
  .sidebar-toggle {
    background: var(--primary-gradient);
    border: none;
    color: white;
    font-weight: var(--font-semibold);
    border-radius: var(--border-radius-small);
    padding: var(--spacing-sm) var(--spacing-md);
    box-shadow: var(--shadow-light);
  }
  
  .chat-sidebar {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 60%;
    max-height: 400px;
    border-right: none;
    border-top: 1px solid var(--border-light);
    transform: translateY(100%);
    transition: transform 0.3s ease;
    z-index: 901;
    box-shadow: var(--shadow-heavy);
    border-radius: var(--border-radius) var(--border-radius) 0 0;
    background: var(--bg-primary);
    backdrop-filter: blur(15px);
  }
  
  .chat-sidebar.sidebar-visible {
    transform: translateY(0);
  }
  
  .chat-sidebar.sidebar-hidden {
    transform: translateY(100%);
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .messages-container {
    padding: var(--spacing-md);
    gap: var(--spacing-sm);
    min-height: 150px;
  }
  
  .message-input-area {
    padding: var(--spacing-md);
    border-radius: 0;
  }
  
  .input-container {
    gap: var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .chat-container {
    height: calc(100vh - 50px);
    margin: var(--spacing-sm);
  }
  
  .chat-sidebar {
    height: 50%;
    max-height: 300px;
  }
  
  .conversation-item {
    padding: var(--spacing-md);
  }
  
  .avatar img {
    width: 36px;
    height: 36px;
  }
  
  .username {
    font-size: var(--font-xs);
  }
  
  .last-message {
    font-size: var(--font-xs);
  }
  
  .chat-header {
    padding: var(--spacing-sm);
  }
  
  .friend-info img {
    width: 28px;
    height: 28px;
  }
  
  .friend-info h4 {
    font-size: var(--font-sm);
  }
  
  .messages-container {
    padding: var(--spacing-sm);
    gap: var(--spacing-sm);
  }
  
  .message-content {
    max-width: 90%;
    padding: var(--spacing-sm) var(--spacing-md);
    border-radius: var(--border-radius-small);
  }
  
  .message-text {
    font-size: var(--font-sm);
  }
  
  .message-time {
    font-size: var(--font-xs);
  }
  
  .message-input-area {
    padding: var(--spacing-sm);
  }
  
  .input-container {
    gap: var(--spacing-xs);
  }
  
  .send-button {
    padding: var(--spacing-xs) var(--spacing-md);
    font-size: var(--font-xs);
  }
}

/* Animations */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}
</style>
