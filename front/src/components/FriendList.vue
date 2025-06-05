<template>
  <div class="friend-list-container">
    <!-- 搜索和好友请求入口 -->
    <div class="search-and-add">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名"
          class="search-input"
          :loading="isSearching"
          @input="handleSearch"
          clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-badge
          :value="friendRequests.length"
          class="request-badge"
          v-if="friendRequests.length > 0"
      >
        <el-button type="warning" @click="showRequestDialog = true">
          好友请求
        </el-button>
      </el-badge>
      <el-button v-else type="info" @click="showRequestDialog = true">
        好友请求
      </el-button>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" class="search-results">
      <div
          v-for="user in searchResults"
          :key="user.id"
          class="search-result-item"
      >
        <div class="search-result-content">
          <div class="user-info">
            <img :src="getAvatarUrl(user.avatarUrl)" :alt="user.username" class="friend-avatar" />
            <span class="friend-username">{{ user.username }}</span>
          </div>
          <el-button
              type="primary"
              size="default"
              @click="sendFriendRequestFromSearch(user)"
              :disabled="isAlreadyFriend(user)"
          >
            {{ isAlreadyFriend(user) ? '已是好友' : '添加' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 好友列表 -->
    <div class="friend-list">        <div
          v-for="friend in filteredFriends"
          :key="friend.id"
          class="friend-item"
          @contextmenu.prevent="showContextMenu($event, friend.id)"
        >
        <div class="friend-info">
          <img :src="getAvatarUrl(friend.avatar)" :alt="friend.username" class="friend-avatar" />
          <span class="friend-username">{{ friend.username }}</span>
          <div class="status-indicator" :class="{ online: friend.online }"></div>
        </div>
        <div class="friend-actions">
          <el-button type="primary" size="small" @click="openChat(friend.id)">
            聊天
          </el-button>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
        v-if="contextMenuVisible"
        :style="{ top: contextMenuPosition.y + 'px', left: contextMenuPosition.x + 'px' }"
        class="custom-context-menu"
    >
      <div class="menu-item delete-item" @click="deleteFriend(contextMenuFriendId)">
        <el-icon><Delete /></el-icon>
        <span>删除好友</span>
      </div>
    </div>

    <!-- 好友请求对话框 -->
    <el-dialog v-model="showRequestDialog" title="好友请求">
      <div v-if="friendRequests.length === 0">暂无好友请求</div>
      <div v-else>
        <div
            v-for="(request, index) in friendRequests"
            :key="request.id"
            class="request-item"
        >
          <div class="request-info">
            <span class="request-username">{{ request.username }} 请求加你为好友</span>
            <span class="request-time">{{ formatTime(request.createdAt) }}</span>
          </div>
          <div class="request-actions">
            <el-button type="success" size="small" @click="acceptRequest(index)">接受</el-button>
            <el-button type="danger" size="small" @click="rejectRequest(index)">拒绝</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { connectWebSocket } from '@/utils/websocket'
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie'

const router = useRouter()

const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)
const defaultAvatar = 'default-avatar.jpg'
const contextMenuVisible = ref(false)

// Helper function to construct avatar URL
const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) return '/default-avatar.png'
  if (avatarPath.startsWith('http')) return avatarPath
  const baseUrl = import.meta.env.VITE_API_BASE
  return `${baseUrl}${avatarPath.startsWith('/') ? avatarPath : '/' + avatarPath}`
}
const contextMenuPosition = reactive({ x: 0, y: 0 })
const contextMenuFriendId = ref(null)

const friends = ref([])
const showRequestDialog = ref(false)
const friendRequests = ref([])

// 时间格式化函数
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 获取好友列表
const getFriendsList = async () => {
  try {
    console.log('开始获取好友列表')
    const res = await axios.get(`${import.meta.env.VITE_API_BASE}/contacts/list`, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    console.log('获取好友列表响应:', res.data)
    if (res.data.code === "200") {  // 统一使用字符串 "200"
      friends.value = res.data.data
      console.log('更新后的好友列表:', friends.value)
    } else {
      throw new Error(res.data.message || '获取好友列表失败')
    }
  } catch (err) {
    console.error('获取好友列表失败:', err)
    ElMessage.error(err.message || '获取好友列表失败')
  }
}

// 获取好友请求
const getFriendRequests = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE}/contacts/requests`, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    if (res.data.code === "200") {
      friendRequests.value = res.data.data
    }
  } catch (err) {
    console.error('获取好友请求失败:', err)
    ElMessage.error('获取好友请求失败')
  }
}

// 搜索处理
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    isSearching.value = true
    const res = await axios.post(`${import.meta.env.VITE_API_BASE}/user/search`, {
      keyword: searchKeyword.value.trim()
    }, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    if (res.data.code === "200") {
      searchResults.value = Array.isArray(res.data.data) ? res.data.data : [res.data.data]
    } else {
      searchResults.value = []
      ElMessage.error(res.data.message || '搜索失败')
    }
  } catch (err) {
    ElMessage.error('搜索接口异常')
    searchResults.value = []
  } finally {
    isSearching.value = false
  }
}

// 过滤好友
const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value
  return friends.value.filter(friend =>
      friend.username.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 是否已是好友
const isAlreadyFriend = (user) => {
  return friends.value.some(f => f.id === user.id)
}

// 发送好友请求
const sendFriendRequestFromSearch = async (user) => {
  // 获取当前用户信息
  const token = Cookies.get('token')
  if (!token) {
    ElMessage.error('未登录')
    return
  }

  try {
    // 获取当前用户信息
    const currentUserRes = await axios.post(`${import.meta.env.VITE_API_BASE}/user/profile`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const currentUser = currentUserRes.data.data

    // 检查是否是添加自己
    if (currentUser.id === user.id) {
      ElMessage.warning('不能添加自己为好友')
      return
    }

    if (isAlreadyFriend(user)) {
      ElMessage.warning('该用户已是你的好友')
      return
    }

    const res = await axios.post(`${import.meta.env.VITE_API_BASE}/contacts/request`, {
      userId: user.id
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.data.code === "200") {
      ElMessage.success('好友请求已发送')
      searchResults.value = searchResults.value.filter(u => u.id !== user.id)
    } else {
      throw new Error(res.data.message)
    }
  } catch (err) {
    console.log(err.message)
    ElMessage.error(err.message || '发送请求失败')
  }
}

// 右键菜单
const showContextMenu = (event, friendId) => {
  event.preventDefault()
  contextMenuFriendId.value = friendId
  contextMenuPosition.x = event.clientX
  contextMenuPosition.y = event.clientY
  contextMenuVisible.value = true

  const handleClickOutside = () => {
    contextMenuVisible.value = false
    document.removeEventListener('click', handleClickOutside)
  }
  document.addEventListener('click', handleClickOutside)
}

// 删除好友
const deleteFriend = async (friendId) => {
  try {
    await ElMessageBox.confirm('确定要删除该好友吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await axios.delete(`${import.meta.env.VITE_API_BASE}/contacts/${friendId}`, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    if (res.data.code === "200") {
      friends.value = friends.value.filter(f => f.id !== friendId)
      ElMessage.success('删除成功')
    } else {
      throw new Error(res.data.message)
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
  contextMenuVisible.value = false
}

// 接受好友请求
const acceptRequest = async (index) => {
  const request = friendRequests.value[index]
  try {
    console.log('接受好友请求:', request)
    const res = await axios.post(`${import.meta.env.VITE_API_BASE}/contacts/requests/accept`, {
      fromUserId: request.userId
    }, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    console.log('接受好友请求响应:', res.data)
    if (res.data.code === "200") {
      ElMessage.success('已添加好友')
      friendRequests.value.splice(index, 1)
      console.log('准备刷新好友列表')
      await getFriendsList()  // 使用 await 确保获取完成
      console.log('好友列表刷新完成')
    } else {
      throw new Error(res.data.message || '处理失败')
    }
  } catch (err) {
    console.error('接受好友请求失败:', err)
    ElMessage.error(err.message || '处理失败')
  }
}

// 拒绝好友请求
const rejectRequest = async (index) => {
  const request = friendRequests.value[index]
  try {
    const res = await axios.post(`${import.meta.env.VITE_API_BASE}/contacts/requests/reject`, {
      fromUserId: request.userId
    }, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    if (res.data.code === 200) {
      friendRequests.value.splice(index, 1)
      ElMessage.info('已拒绝好友请求')
    } else {
      throw new Error(res.data.message)
    }
  } catch (err) {
    ElMessage.error(err.message || '处理失败')
  }
}

// 打开聊天
const openChat = (friendId) => {
  // 存储当前选择的好友ID到sessionStorage
  sessionStorage.setItem('selectedFriendId', friendId)
  
  // 跳转到聊天页面并传递 friendId
  router.push({
    path: '/app/chat',
    query: { friendId: friendId }
  })
}

// WebSocket 实时推送
onMounted(() => {
  getFriendsList()
  getFriendRequests()

  const token = Cookies.get('token')
  if (!token) {
    ElMessage.error('未登录，无法建立WebSocket连接')
    return
  }

  connectWebSocket(token, (msg) => {
    console.log('收到WebSocket消息:', msg)

    try {
      const message = typeof msg === 'string' ? JSON.parse(msg) : msg

      if (message.type === 'FRIEND_REQUEST') {
        console.log('收到好友请求:', message.data)
        getFriendRequests()
        ElMessage.info(`收到来自 ${message.data.username} 的好友请求`)
      }
      if (message.type === 'FRIEND_RESPONSE') {
        console.log('收到好友响应:', message.data)
        if (message.data.accepted) {
          ElMessage.success(`${message.data.username} 接受了你的好友请求`)
          getFriendsList()
        } else {
          ElMessage.info(`${message.data.username} 拒绝了你的好友请求`)
        }
      }
    } catch (error) {
      console.error('处理WebSocket消息出错:', error)
    }
  })
})
</script>

<style scoped>
.friend-list-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-and-add {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #e0e0e0;
  gap: 10px;
}

.search-input {
  flex: 1;
}

.friend-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.friend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.friend-item:hover {
  background-color: #f5f5f5;
}

.friend-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.friend-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 25px;
}

.friend-username {
  font-size: 20px !important;
  line-height: 40px;
  display: inline-block;
  vertical-align: middle;
}

.status-indicator {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  margin-left: 10px;
  background-color: #ccc;
}

.status-indicator.online {
  background-color: #4caf50;
}

.friend-actions {
  margin-left: 10px;
}

.custom-context-menu {
  position: fixed;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  min-width: 120px;
  z-index: 1000;
  animation: menuFadeIn 0.2s ease;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #606266;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.delete-item {
  color: #f56c6c;
}

.delete-item:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.request-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.request-username {
  font-size: 14px;
  color: #333;
}

.request-time {
  font-size: 12px;
  color: #999;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.search-results {
  padding: 10px;
}

.search-result-item {
  margin-bottom: 10px;
}

.search-result-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Mobile responsive styles */
@media (max-width: 768px) {
  .friend-list-container {
    margin: 10px;
    border-radius: 8px;
  }
  
  .search-and-add {
    padding: 15px;
    flex-direction: column;
    gap: 15px;
  }
  
  .search-input {
    order: 1;
  }
  
  .friend-list {
    max-height: calc(100vh - 200px);
    padding: 5px;
  }
  
  .friend-item {
    padding: 15px 10px;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .friend-info {
    width: 100%;
  }
  
  .friend-avatar {
    width: 45px;
    height: 45px;
    margin-right: 15px;
  }
  
  .friend-username {
    font-size: 18px !important;
  }
  
  .friend-actions {
    width: 100%;
    margin-left: 0;
    display: flex;
    justify-content: flex-end;
  }
  
  .search-result-content {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .user-info {
    width: 100%;
  }
  
  .request-item {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .request-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .friend-list-container {
    margin: 5px;
  }
  
  .search-and-add {
    padding: 10px;
  }
  
  .friend-list {
    max-height: calc(100vh - 180px);
  }
  
  .friend-item {
    padding: 12px 8px;
  }
  
  .friend-avatar {
    width: 40px;
    height: 40px;
    margin-right: 12px;
  }
  
  .friend-username {
    font-size: 16px !important;
  }
  
  .status-indicator {
    width: 12px;
    height: 12px;
  }
  
  .search-result-content {
    padding: 12px;
  }
  
  .custom-context-menu {
    min-width: 140px;
    font-size: 14px;
  }
  
  .menu-item {
    padding: 10px 14px;
  }
}
</style>