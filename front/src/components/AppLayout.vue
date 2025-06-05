<!-- AppLayout.vue -->
<script setup>
import { useRoute } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import router from "@/router/index.js"
import axios from "axios"
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useChatStore } from '@/store/chat'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

const route = useRoute()
const chatStore = useChatStore()

// 页面跳转
const logout = () => {
  // Cleanup chat when logging out
  chatStore.cleanup()
  
  // 清除认证信息
  Cookies.remove('token')
  Cookies.remove('userId')
  sessionStorage.clear()
  localStorage.clear()
  
  ElMessage.info('已安全退出')
  router.push('/login')
}

const passwordchange = () => router.push('/app/passwordChange')
const personalinfo = () => router.push('/app/person')

// 用户信息
const userInfo = ref(null)

const getUserInfo = async () => {
  // 直接从cookie获取token
  const token = Cookies.get('token')
  if (token) {
    try {      const response = await axios.post(`${import.meta.env.VITE_API_BASE}/user/profile`, {}, {
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
      sessionStorage.setItem('userId', response.data.data.id)
      sessionStorage.setItem('username', response.data.data.username)
      
      // Also store in cookies for compatibility
      Cookies.set('userId', response.data.data.id)
    } catch (error) {
      console.error('获取个人信息失败:', error)
    }
  }
}

const user = computed(() => userInfo.value || {})

onMounted(async () => {
  await getUserInfo()
  
  // Initialize chat system if user is logged in
  const token = Cookies.get('token')
  if (token) {
    try {
      await chatStore.connectWebSocket()
    } catch (error) {
      console.error('Failed to initialize chat system:', error)
    }
  }
})

onUnmounted(() => {
  // Cleanup when app layout unmounts
  chatStore.cleanup()
})
</script>

<template>
  <el-container>    <!-- 侧边栏 -->
    <el-aside class="app-sidebar" style="width: 240px; min-height: 100vh; background-color: #001529;">
      <div style="height: 80px; color: azure; display: flex; align-items: center; justify-content: center">
        <img src="@/assets/css/img_2.png" style="width:50px;height: 50px; border-radius: 50%; object-fit: cover;">
        <span style="padding: 15px; font-size: 30px">聊天系统</span>
      </div>

      <!-- 菜单栏 -->
      <el-menu
          router
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
          style="border: none;"
          :default-active="route.path"
      >        <el-menu-item index="/app/home">系统首页</el-menu-item>

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
        <el-breadcrumb :separator-icon="ArrowRight" style="margin-left: 20px;font-size: 20px;">
          <template v-for="(item, index) in route.matched" :key="index">
            <el-breadcrumb-item v-if="index === route.matched.length - 1 && item.meta.parentTitle">
              {{ item.meta.parentTitle }}
            </el-breadcrumb-item>
            <el-breadcrumb-item :to="item.path">
              {{ item.meta.title }}
            </el-breadcrumb-item>
          </template>
        </el-breadcrumb>

        <!-- 用户下拉 -->
        <div style="flex: 1; display: flex; align-items: center; justify-content: flex-end">
          <el-dropdown placement="bottom">
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="personalinfo">个人信息</el-dropdown-item>
                <el-dropdown-item @click="passwordchange">修改密码</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
            <div style="display: flex; align-items: center; cursor: pointer">
              <img
                  :src="user.avatarUrl || '/default-avatar.png'"
                  style="width:50px;height:50px; border-radius: 50%; object-fit: cover;"
              >
              <span style="font-size: 20px; padding-left: 10px;">
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
:deep(.el-aside) {
  box-shadow: 1px 0 6px rgba(0, 21, 41, 0.35);
}
:deep(.el-header) {
  box-shadow: 1px 0 6px rgba(0, 21, 41, 0.35);
  height: 80px !important;
  display: flex;
  align-items: center;
}
/* 菜单样式 */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  height: 60px;
  line-height: 48px;
  padding-left: 80px !important;
  display: flex;
  align-items: center;
  transition: background-color 0.1s, color 0.1s;
}
:deep(.el-menu--inline .el-menu-item) {
  font-size: 16px;
  padding-left: 85px !important;
  height: 44px;
  line-height: 44px;
  background-color: #001d3a;
}
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover),
:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: #003a8c !important;
  color: #fff !important;
}
:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
  border-radius: 6px;
  margin: 4px 4px;
}
.el-aside {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
}
:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.6);
}

/* Mobile responsive styles */
@media (max-width: 768px) {
  .el-container {
    flex-direction: column;
  }
  
  .app-sidebar {
    width: 100% !important;
    height: auto !important;
    min-height: auto !important;
    order: 2;
  }
  
  .app-header {
    height: 60px !important;
    order: 1;
    padding: 0 15px !important;
  }
  
  .app-main {
    order: 3;
    padding: 10px !important;
  }
  
  :deep(.el-aside) {
    box-shadow: 0 -1px 6px rgba(0, 21, 41, 0.35);
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    font-size: 16px;
    height: 50px;
    line-height: 40px;
    padding-left: 60px !important;
  }
  
  :deep(.el-menu--inline .el-menu-item) {
    font-size: 14px;
    padding-left: 65px !important;
    height: 40px;
    line-height: 40px;
  }
  
  .app-sidebar .el-menu {
    display: flex;
    overflow-x: auto;
    white-space: nowrap;
  }
  
  .app-sidebar .el-menu-item,
  .app-sidebar .el-sub-menu {
    flex-shrink: 0;
    min-width: 120px;
  }
  
  /* Header logo area */
  .app-sidebar > div:first-child {
    height: 60px;
    font-size: 20px;
  }
  
  .app-sidebar > div:first-child img {
    width: 35px;
    height: 35px;
  }
  
  .app-sidebar > div:first-child span {
    padding: 10px;
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .app-header {
    height: 50px !important;
    padding: 0 10px !important;
  }
  
  .app-main {
    padding: 5px !important;
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    font-size: 14px;
    height: 45px;
    line-height: 36px;
    padding-left: 40px !important;
  }
  
  .app-sidebar .el-menu-item,
  .app-sidebar .el-sub-menu {
    min-width: 100px;
  }
  
  /* Header logo area */
  .app-sidebar > div:first-child {
    height: 50px;
    font-size: 16px;
  }
  
  .app-sidebar > div:first-child img {
    width: 30px;
    height: 30px;
  }
  
  .app-sidebar > div:first-child span {
    padding: 8px;
    font-size: 16px;
  }
  
  /* User dropdown */
  .app-header .el-dropdown img {
    width: 35px !important;
    height: 35px !important;
  }
  
  .app-header .el-dropdown span {
    font-size: 16px !important;
    padding-left: 8px !important;
  }
  
  /* Breadcrumb */
  .app-header .el-breadcrumb {
    margin-left: 10px !important;
    font-size: 14px !important;
  }
}
</style>
