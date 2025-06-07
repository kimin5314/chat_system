<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/store/chat'
import { useFriendStore } from '@/store/friend'
import { 
  ChatDotRound, 
  User, 
  Files, 
  UserFilled, 
  Folder,
  Message,
  Clock,
  Bell
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import Cookies from 'js-cookie'

const router = useRouter()
const chatStore = useChatStore()
const friendStore = useFriendStore()

// Dashboard data
const stats = ref({
  todayMessages: 0,
  onlineFriends: 0,
  totalFiles: 0,
  unreadMessages: 0
})

const recentConversations = ref([])
const userInfo = ref({})

// Computed properties
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const onlineFriendsCount = computed(() => {
  return friendStore.friends.filter(f => f.online).length
})

const unreadMessagesCount = computed(() => {
  return chatStore.conversations.reduce((total, conv) => 
    total + (conv.unreadCount || 0), 0)
})

// Reactive file count based on actual data
const fileCount = computed(() => {
  // Update when stats change
  return stats.value.totalFiles
})

// Reactive today's messages count
const todayMessageCount = computed(() => {
  return stats.value.todayMessages
})

const currentTime = ref(new Date().toLocaleString('zh-CN'))

// Methods
const goToChat = () => router.push('/app/chat')
const goToFriends = () => router.push('/app/friendList')
const goToFiles = () => router.push('/app/fileList')
const goToProfile = () => router.push('/app/person')

const openChat = (friendId) => {
  router.push(`/app/chat/${friendId}`)
}

const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) return 'https://avatars.githubusercontent.com/u/583231?v=4'
  if (avatarPath.startsWith('http')) return avatarPath
  const baseUrl = import.meta.env.VITE_API_BASE
  return `${baseUrl}${avatarPath.startsWith('/') ? avatarPath : '/' + avatarPath}`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// Load dashboard data
const loadDashboardData = async () => {
  try {
    // Get user info using the correct API endpoint
    const userResponse = await request.post('/user/profile', {}, {
      headers: { 'Authorization': `Bearer ${Cookies.get('token')}` }
    })
    userInfo.value = userResponse.data.data

    // Get friends list (online status will be updated via WebSocket)
    await friendStore.getFriendsList()

    // Get recent conversations
    await chatStore.getConversations()
    recentConversations.value = chatStore.conversations.slice(0, 5)

    // Get file count from the correct API endpoint
    try {
      const filesResponse = await request.get('/file/list', {
        headers: { 'Authorization': `Bearer ${Cookies.get('token')}` }
      })
      if (filesResponse.data && filesResponse.data.data) {
        // Count total files from the response
        stats.value.totalFiles = filesResponse.data.data.length || 0
      } else {
        stats.value.totalFiles = 0
      }
    } catch (error) {
      console.log('Could not load file count:', error)
      stats.value.totalFiles = 0
    }    // Get real message statistics
    try {
      const today = new Date().toISOString().split('T')[0]
      const messagesResponse = await request.get('/messages/stats', {
        headers: { 'Authorization': `Bearer ${Cookies.get('token')}` },
        params: { date: today }
      })
      if (messagesResponse.data.code === '200') {
        stats.value.todayMessages = messagesResponse.data.data.count || 0
      } else {
        // Fallback: count messages from conversations for today
        const todayMessages = chatStore.conversations.reduce((total, conv) => {
          // This is a simplified count - you might want to implement proper message counting
          return total + (conv.lastMessageTime && 
            new Date(conv.lastMessageTime).toDateString() === new Date().toDateString() ? 1 : 0)
        }, 0)
        stats.value.todayMessages = todayMessages
      }
    } catch (error) {
      console.log('Could not load message stats:', error)
      // Fallback: use conversation count as approximation
      stats.value.todayMessages = chatStore.conversations.length
    }

  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

// Update time every minute
const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

// Refresh dashboard data periodically
const refreshData = async () => {
  await loadDashboardData()
}

onMounted(() => {
  loadDashboardData()
  
  // Update time every minute
  const timeInterval = setInterval(updateTime, 60000)
  
  // Refresh data every 5 minutes
  const dataInterval = setInterval(refreshData, 300000)
  
  // Cleanup intervals on unmount
  return () => {
    clearInterval(timeInterval)
    clearInterval(dataInterval)
  }
})
</script>

<template>
  <div class="dashboard-container">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div class="welcome-section">
        <h1>{{ greeting }}，{{ userInfo.username || '用户' }}！</h1>
        <p class="current-time">
          <el-icon><Clock /></el-icon>
          {{ currentTime }}
        </p>
      </div>
      <div class="user-avatar-section">
        <img 
          :src="getAvatarUrl(userInfo.avatar)" 
          :alt="userInfo.username"
          class="dashboard-avatar"
          @click="goToProfile"
        >
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="stats-grid">
      <div class="stat-card messages">
        <div class="stat-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>        <div class="stat-content">
          <h3>{{ todayMessageCount }}</h3>
          <p>今日消息</p>
        </div>
      </div>
        <div class="stat-card friends">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ onlineFriendsCount }}</h3>
          <p>在线好友</p>
        </div>
      </div>
      
      <div class="stat-card files">
        <div class="stat-icon">
          <el-icon><Files /></el-icon>
        </div>        <div class="stat-content">
          <h3>{{ fileCount }}</h3>
          <p>共享文件</p>
        </div>
      </div>      <div class="stat-card unread">
        <div class="stat-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ unreadMessagesCount }}</h3>
          <p>未读消息</p>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions-section">
      <h3>快速操作</h3>
      <div class="action-grid">
        <div class="action-card" @click="goToChat">
          <div class="action-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="action-content">
            <h4>开始聊天</h4>
            <p>与好友即时沟通</p>
          </div>
        </div>

        <div class="action-card" @click="goToFriends">
          <div class="action-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="action-content">
            <h4>管理好友</h4>
            <p>添加或管理好友</p>
          </div>
        </div>

        <div class="action-card" @click="goToFiles">
          <div class="action-icon">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="action-content">
            <h4>文件管理</h4>
            <p>上传和分享文件</p>
          </div>
        </div>

        <div class="action-card" @click="goToProfile">
          <div class="action-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="action-content">
            <h4>个人资料</h4>
            <p>编辑个人信息</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Conversations -->
    <div class="recent-section">
      <h3>最近对话</h3>
      <div class="conversation-list">
        <div 
          v-for="conversation in recentConversations" 
          :key="conversation.friendId"
          class="conversation-item"
          @click="openChat(conversation.friendId)"
        >
          <div class="conversation-avatar">
            <img :src="getAvatarUrl(conversation.friendAvatar)" :alt="conversation.friendUsername">
            <div v-if="conversation.isOnline" class="online-dot"></div>
          </div>
          <div class="conversation-info">
            <div class="conversation-header">
              <span class="conversation-name">{{ conversation.friendUsername }}</span>
              <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
            </div>
            <div class="conversation-message">
              {{ conversation.lastMessage || '开始新对话...' }}
            </div>
            <div v-if="conversation.unreadCount > 0" class="unread-indicator">
              {{ conversation.unreadCount }}
            </div>
          </div>
        </div>
        
        <div v-if="recentConversations.length === 0" class="no-conversations">
          <el-icon><Message /></el-icon>
          <p>还没有对话记录</p>
          <el-button type="primary" @click="goToChat">开始第一次聊天</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: var(--spacing-xl);
  max-width: 1200px;
  margin: 0 auto;
  animation: slideUp 0.5s ease;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3xl);
  padding: var(--spacing-xl);
  background: var(--primary-gradient);
  border-radius: var(--border-radius);
  color: white;
  box-shadow: var(--shadow-medium);
}

.welcome-section h1 {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: var(--font-size-3xl);
  font-weight: 600;
}

.current-time {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0;
  font-size: var(--font-size-base);
  opacity: 0.9;
}

.dashboard-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-light);
}

.dashboard-avatar:hover {
  transform: scale(1.1);
  border-color: rgba(255, 255, 255, 0.6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-medium);
}

.stat-card.messages .stat-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-card.friends .stat-icon {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-card.files .stat-icon {
  background: linear-gradient(135deg, #17a2b8, #6f42c1);
}

.stat-card.unread .stat-icon {
  background: linear-gradient(135deg, #dc3545, #fd7e14);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-xl);
}

.stat-content h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.stat-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.quick-actions-section {
  margin-bottom: var(--spacing-3xl);
}

.quick-actions-section h3 {
  margin-bottom: var(--spacing-lg);
  color: var(--text-primary);
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.action-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
  border-color: var(--primary-color);
}

.action-icon {
  width: 50px;
  height: 50px;
  border-radius: var(--border-radius-small);
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-lg);
}

.action-content h4 {
  margin: 0 0 var(--spacing-xs) 0;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  font-weight: 600;
}

.action-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.recent-section h3 {
  margin-bottom: var(--spacing-lg);
  color: var(--text-primary);
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.conversation-list {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background: var(--bg-hover);
}

.conversation-avatar {
  position: relative;
}

.conversation-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: var(--shadow-light);
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: var(--success-color);
  border: 2px solid var(--bg-primary);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(40, 167, 69, 0.5);
}

.conversation-info {
  flex: 1;
  min-width: 0;
  position: relative;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.conversation-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.conversation-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.conversation-message {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-indicator {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--danger-color);
  color: white;
  border-radius: var(--border-radius);
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.no-conversations {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--text-secondary);
}

.no-conversations .el-icon {
  font-size: var(--font-size-3xl);
  color: var(--text-muted);
  margin-bottom: var(--spacing-lg);
}

.no-conversations p {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: var(--font-size-base);
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .dashboard-container {
    padding: var(--spacing-md);
  }
  
  .dashboard-header {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-lg);
  }
  
  .dashboard-avatar {
    width: 60px;
    height: 60px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--spacing-md);
  }
  
  .action-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .stat-card,
  .action-card {
    padding: var(--spacing-lg);
  }
  
  .conversation-item {
    padding: var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: var(--spacing-sm);
    margin: var(--spacing-sm);
  }
  
  .dashboard-header {
    padding: var(--spacing-md);
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-md);
  }
  
  .welcome-section h1 {
    font-size: var(--font-size-xl);
    margin-bottom: var(--spacing-xs);
  }
  
  .current-time {
    font-size: var(--font-size-sm);
  }
  
  .dashboard-avatar {
    width: 60px;
    height: 60px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-sm);
  }
  
  .stat-card {
    gap: var(--spacing-sm);
    padding: var(--spacing-md);
  }
 
  .stat-card h3 {
    font-size: var(--font-size-lg);
  }
  
  .stat-card p {
    font-size: var(--font-size-xs);
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }
  
  .action-card {
    padding: var(--spacing-md);
    gap: var(--spacing-sm);
  }
  
  .action-icon {
    width: 36px;
    height: 36px;
  }
  
  .recent-section {
    padding: var(--spacing-md);
  }
  
  .conversation-item {
    padding: var(--spacing-sm);
  }
  
  .conversation-item .avatar {
    width: 36px;
    height: 36px;
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
</style>