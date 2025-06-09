<template>
  <div class="login-container">
    <div class="login-box">
      <h2>欢迎登录</h2>
      <div class="input-group">
        <label for="username">用户名：</label>
        <input
            type="text"
            id="username"
            v-model="username"
            placeholder="请输入用户名"
        />
      </div>
      <div class="input-group">
        <label for="password">密码：</label>
        <input
            type="password"
            id="password"
            v-model="password"
            placeholder="请输入密码"
        />
      </div>
      <button class="login-button" @click="login">登录</button>
      <div class="extra-links">
        <router-link to="/register" class="register-link">还没有账号？去注册</router-link>
        <span class="forgot-password" @click="handleForgotPassword">忘记密码？</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import Cookies from 'js-cookie';
import { useE2EEStore } from '@/store/e2ee';

const router = useRouter();
const e2eeStore = useE2EEStore();
const username = ref('');
const password = ref('');

let socket = null; // 保存 WebSocket 实例

const login = async () => {
  if (!username.value) {
    ElMessage.error('用户名不能为空');
    return;
  }
  if (!password.value) {
    ElMessage.error('密码不能为空');
    return;
  }  try {
    const response = await request.post('/user/login', {
      username: username.value,
      password: password.value
    });

    const res = response.data;    if (res.code === "200" && res.data?.token) {
      // 直接设置cookie
      Cookies.set('token', res.data.token, { expires: 7 });
      Cookies.set('userId', res.data.userId || username.value, { expires: 7 });
        ElMessage.success('登录成功');

      // 初始化 WebSocket 连接
      initWebSocket(res.data.token);

      // Initialize E2EE system after login to ensure keys are properly loaded
      try {
        console.log('🔑 Reinitializing E2EE after login...');
        await e2eeStore.reinitialize();
        console.log('✅ E2EE reinitialized successfully after login');
      } catch (error) {
        console.warn('⚠️ E2EE reinitialization failed (non-critical):', error);
      }

      await router.push('/app');
    } else {
      // 处理业务逻辑错误
      if (res.code === "400") {
        ElMessage.error('用户名或密码错误');
      } else if (res.code === "404") {
        ElMessage.error('用户不存在');
      } else if (res.code === "403") {
        ElMessage.error('账户已被禁用');
      } else {
        ElMessage.error(res.msg || res.message || '登录失败，请检查用户名或密码');
      }
    }
  } catch (error) {
    console.error('登录错误:', error);
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status;
      const errorData = error.response.data;
      
      if (status === 400) {
        ElMessage.error(errorData.message || '用户名或密码错误');
      } else if (status === 401) {
        ElMessage.error('认证失败，请检查用户名和密码');
      } else if (status === 404) {
        ElMessage.error('用户不存在');
      } else if (status === 500) {
        ElMessage.error('服务器内部错误，请稍后重试');
      } else {
        ElMessage.error(`登录失败 (${status}): ${errorData.message || errorData.msg || '未知错误'}`);
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络连接失败，请检查网络连接后重试');
    } else {
      // 其他错误
      ElMessage.error('登录失败: ' + (error.message || '未知错误'));
    }
  }
};

const initWebSocket = (token) => {
  const wsUrl = `${import.meta.env.VITE_WS_URL}?token=${token}`;
  socket = new WebSocket(wsUrl);
  window.$socket = socket; // 可全局访问

  socket.onopen = () => {
    console.log("WebSocket 连接成功");
  };
  socket.onmessage = (event) => {
    console.log("收到 WebSocket 消息:", event.data);
    // Removed debugging popup notification
    // ElMessage.info(`通知：${event.data}`);
  };

  socket.onclose = () => {
    console.log("WebSocket 连接关闭");
  };

  socket.onerror = (err) => {
    console.error("WebSocket 连接错误", err);
  };
};

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员或通过预留邮箱重置密码。');
};
</script>



<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-light);
  padding: var(--spacing-lg);
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: var(--spacing-4xl);
  background: var(--bg-primary);
  box-shadow: var(--shadow-medium);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
  animation: slideUp 0.5s ease;
}

h2 {
  text-align: center;
  margin-bottom: var(--spacing-3xl);
  color: var(--text-primary);
  font-size: var(--font-size-2xl);
  font-weight: 600;
}

.input-group {
  margin-bottom: var(--spacing-xl);
}

.input-group label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.input-group input {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-small);
  font-size: var(--font-size-base);
  transition: all var(--transition-fast);
  background: var(--bg-primary);
  color: var(--text-primary);
}

.input-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-button {
  width: 100%;
  padding: var(--spacing-md);
  background: var(--primary-gradient);
  color: white;
  border: none;
  border-radius: var(--border-radius-small);
  cursor: pointer;
  font-size: var(--font-size-base);
  font-weight: 600;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.extra-links {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-lg);
  gap: var(--spacing-md);
}

.register-link,
.forgot-password {
  color: var(--primary-color);
  text-decoration: none;
  font-size: var(--font-size-sm);
  font-weight: 500;
  transition: all var(--transition-fast);
  padding: var(--spacing-xs);
  border-radius: var(--border-radius-small);
}

.register-link:hover,
.forgot-password:hover {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
  transform: translateY(-1px);
}

@media (max-width: 480px) {
  .login-box {
    padding: var(--spacing-2xl);
  }
  
  .extra-links {
    flex-direction: column;
    gap: var(--spacing-sm);
    text-align: center;
  }
}
</style>