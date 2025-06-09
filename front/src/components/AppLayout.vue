<!-- AppLayout.vue -->
<script setup>
import { useRoute } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import router from "@/router/index.js"
import request from '@/utils/request'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useChatStore } from '@/store/chat'
import { useE2EEStore } from '@/store/e2ee'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'
import { Menu } from '@element-plus/icons-vue'
import { clearLocalStoragePreservingE2EE } from '@/utils/storage'

const route = useRoute()
const chatStore = useChatStore()
const e2eeStore = useE2EEStore()

// Mobile responsiveness
const isMobile = ref(false)
const sidebarVisible = ref(false)

function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) sidebarVisible.value = false
}

// 页面跳转
const logout = () => {
  // Cleanup chat when logging out
  chatStore.cleanup()
  
  // Clear authentication and other data while preserving E2EE keys
  Cookies.remove('token')
  Cookies.remove('userId')
  sessionStorage.clear()
  clearLocalStoragePreservingE2EE()
  
  ElMessage.info('已安全退出')
  router.push('/login')
}

const passwordchange = () => router.push('/app/passwordChange')
const personalinfo = () => router.push('/app/person')

// 用户信息
const userInfo = ref({
  username: '未登录',
  avatarUrl: 'https://avatars.githubusercontent.com/u/583231?v=4'
})

const getUserInfo = async () => {
  // 直接从cookie获取token
  const token = Cookies.get('token')
  if (token) {
    try {
      const response = await request.post('/user/profile', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      const user = response.data.data
      
      // Handle avatar URL - if it starts with /uploads, prepend the API base URL
      if (user.avatarUrl && user.avatarUrl.startsWith('/uploads')) {
        user.avatarUrl = `${import.meta.env.VITE_API_BASE}${user.avatarUrl}`
      } else if (!user.avatarUrl) {
        user.avatarUrl = 'https://avatars.githubusercontent.com/u/583231?v=4'
      }
      
      userInfo.value = user
      
      // Store user info for this tab session
      sessionStorage.setItem('userId', user.id)
      sessionStorage.setItem('username', user.username)
      
      // Also store in cookies for compatibility
      Cookies.set('userId', user.id)
    } catch (error) {
      console.error('获取个人信息失败:', error)
      // Set default user info to prevent undefined errors
      userInfo.value = {
        username: '未登录',
        avatarUrl: 'https://avatars.githubusercontent.com/u/583231?v=4'
      }
    }
  } else {
    // Set default user info when no token
    userInfo.value = {
      username: '未登录',
      avatarUrl: 'https://avatars.githubusercontent.com/u/583231?v=4'
    }
  }
}

const user = computed(() => userInfo.value || { 
  username: '未登录', 
  avatarUrl: 'https://avatars.githubusercontent.com/u/583231?v=4' 
})

onMounted(async () => {
  await getUserInfo()
  
  // Initialize chat system if user is logged in
  const token = Cookies.get('token')
  const userId = Cookies.get('userId')
    if (token && userId) {
    try {
      console.log('Initializing chat WebSocket connection for user:', userId)
      const connected = await chatStore.connectWebSocket()
      if (connected) {
        console.log('Chat WebSocket successfully connected')
      } else {
        console.warn('Chat WebSocket connection failed')
      }

      // Initialize E2EE system if user is logged in
      try {
        await e2eeStore.initialize()
      } catch (error) {
        console.warn('E2EE initialization failed (non-critical):', error)
      }
    } catch (error) {
      console.error('Failed to initialize chat system:', error)
    }
  } else {
    console.warn('No token or userId found, skipping WebSocket connection')
  }

  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  // Cleanup when app layout unmounts
  chatStore.cleanup()
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <el-container>
    <!-- 侧边栏 -->
    <!-- Mobile backdrop -->
    <div v-if="isMobile && sidebarVisible" class="mobile-backdrop" @click="toggleSidebar"></div>    <el-aside :class="['app-sidebar', { 'sidebar-hidden': isMobile && !sidebarVisible }]" class="sidebar">
      <div class="sidebar-header">
        <img src="@/assets/css/img_2.png" class="sidebar-logo">
        <span class="sidebar-title">聊天系统</span>
      </div>

      <!-- 菜单栏 -->
      <el-menu
          router
          :background-color="'transparent'"
          text-color="var(--text-sidebar)"
          active-text-color="var(--text-primary)"
          class="sidebar-menu"
          :default-active="route.path"
      ><el-menu-item index="/app/home">系统首页</el-menu-item>

        <el-menu-item index="/app/friendList">好友列表</el-menu-item>
        <el-menu-item index="/app/chat">聊天</el-menu-item>
        <el-menu-item index="/app/fileList">文件管理</el-menu-item>

        <el-sub-menu index="info" class="info-menu">
          <template #title>
            <span>信息管理</span>
          </template>
          <el-menu-item index="/app/person">个人信息</el-menu-item>
          <el-menu-item index="/app/passwordChange">密码修改</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container>      <!-- 顶部导航 -->
      <el-header class="app-header">
        <!-- Mobile menu button in header -->
        <el-button v-if="isMobile" @click="toggleSidebar" class="mobile-menu-button" type="text">
          <el-icon><Menu /></el-icon>
        </el-button>
        <el-breadcrumb :separator-icon="ArrowRight" class="header-breadcrumb">
          <template v-for="(item, index) in route.matched" :key="index">
            <el-breadcrumb-item v-if="index === route.matched.length - 1 && item.meta.parentTitle">
              {{ item.meta.parentTitle }}
            </el-breadcrumb-item>
            <el-breadcrumb-item :to="item.path">
              {{ item.meta.title }}
            </el-breadcrumb-item>
          </template>
        </el-breadcrumb>        <!-- 用户下拉 -->
        <div class="header-user-section">
          <el-dropdown placement="bottom">
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="personalinfo">个人信息</el-dropdown-item>
                <el-dropdown-item @click="passwordchange">修改密码</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
            <div class="user-info">
              <img
                  :src="user.avatarUrl || 'https://avatars.githubusercontent.com/u/583231?v=4'"
                  class="user-avatar"
              >
              <span class="user-name">
                {{ user.username || '未登录' }}
              </span>
            </div>
          </el-dropdown>
        </div>
      </el-header>      <!-- 页面内容 -->
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* Sidebar Styles */
.sidebar {
  width: 240px;
  min-height: 100vh;
  background: var(--sidebar-bg);
  box-shadow: var(--shadow-medium);
}

.sidebar-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--sidebar-header-bg);
  border-bottom: 1px solid var(--border-light);
}

.sidebar-logo {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: var(--shadow-light);
}

.sidebar-title {
  padding: var(--spacing-md);
  font-size: var(--font-size-2xl);
  color: var(--text-sidebar-title);
  font-weight: 600;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

/* Header Styles */
.app-header {
  height: 80px !important;
  display: flex;
  align-items: center;
  background: var(--bg-primary);
  box-shadow: var(--shadow-light);
  border-bottom: 1px solid var(--border-light);
  padding: 0 var(--spacing-lg);
}

.mobile-menu-button {
  margin-right: var(--spacing-md);
  color: var(--text-primary);
}

.header-breadcrumb {
  margin-left: var(--spacing-lg);
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
}

.header-user-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-small);
  transition: all var(--transition-fast);
}

.user-info:hover {
  background: var(--bg-hover);
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: var(--shadow-light);
}

.user-name {
  font-size: var(--font-size-lg);
  padding-left: var(--spacing-sm);
  color: var(--text-primary);
  font-weight: 500;
}

.app-main {
  background: var(--bg-light);
  min-height: calc(100vh - 80px);
}

/* Element Plus Overrides */
:deep(.el-aside) {
  box-shadow: var(--shadow-medium);
}

:deep(.el-header) {
  box-shadow: var(--shadow-light);
  border-bottom: 1px solid var(--border-light);
}

/* Menu Item Styles */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  font-size: var(--font-size-lg);
  color: var(--text-sidebar) !important;
  height: 60px;
  line-height: 48px;
  padding-left: 60px !important;
  display: flex;
  align-items: center;
  transition: all var(--transition-fast);
  margin: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-small);
}

:deep(.el-menu--inline .el-menu-item) {
  font-size: var(--font-size-base);
  padding-left: 80px !important;
  height: 44px;
  line-height: 44px;
  background-color: var(--sidebar-submenu-bg);
  margin: 2px var(--spacing-sm);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover),
:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: var(--sidebar-hover) !important;
  color: var(--text-sidebar-hover) !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
  color: white !important;
  border-radius: var(--border-radius-small);
  box-shadow: var(--shadow-light);
}

:deep(.el-sub-menu__icon-arrow) {
  color: var(--text-sidebar-secondary);
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .app-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 85%;
    max-width: 240px;
    height: 100vh;
    z-index: 999;
    transform: translateX(0);
    transition: transform var(--transition-medium);
  }
  
  .app-sidebar.sidebar-hidden {
    transform: translateX(-100%);
  }
  
  .mobile-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    z-index: 998;
    backdrop-filter: blur(2px);
  }
  
  .app-header {
    position: sticky;
    top: 0;
    z-index: 1000;
    padding: 0 var(--spacing-md);
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    font-size: var(--font-size-base);
    padding-left: var(--spacing-lg) !important;
    height: 50px;
    line-height: 50px;
  }
}

@media (max-width: 480px) {
  .app-header {
    height: 60px !important;
    padding: 0 var(--spacing-sm) !important;
  }
  
  .app-main {
    padding: var(--spacing-xs) !important;
  }
  
  .sidebar-header {
    height: 60px;
  }
  
  .sidebar-logo {
    width: 35px;
    height: 35px;
  }
  
  .sidebar-title {
    font-size: var(--font-size-lg);
    padding: var(--spacing-sm);
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    font-size: var(--font-size-sm);
    height: 45px;
    line-height: 36px;
    padding-left: var(--spacing-lg) !important;
  }
  
  .user-avatar {
    width: 35px !important;
    height: 35px !important;
  }
  
  .user-name {
    font-size: var(--font-size-base) !important;
    padding-left: var(--spacing-xs) !important;
  }
  
  .header-breadcrumb {
    margin-left: var(--spacing-sm) !important;
    font-size: var(--font-size-sm) !important;
  }
}
</style>
