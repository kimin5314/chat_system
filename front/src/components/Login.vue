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
import axios from 'axios';
import { ElMessage } from 'element-plus';
import Cookies from 'js-cookie';

const router = useRouter();
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
  }
  try {
    const response = await axios.post(`${import.meta.env.VITE_API_BASE}/user/login`, {
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

      await router.push('/app');
    } else {
      ElMessage.error(res.msg || '登录失败，请检查用户名或密码');
    }
  } catch (error) {
    if (error.response && error.response.data) {
      ElMessage.error(`登录失败，错误码：${error.response.status}，错误信息：${error.response.data.message}`);
    } else {
      console.error('网络错误', error);
      ElMessage.error('网络错误，请检查网络连接');
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
    ElMessage.info(`通知：${event.data}`);
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
/* 样式代码保持不变 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f4;
}

.login-box {
  width: 350px;
  padding: 40px;
  background-color: #fff;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border-radius: 5px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.input-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 3px;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.login-button:hover {
  background-color: #0056b3;
}

.extra-links {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.register-link {
  color: #007bff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #0056b3;
}

.forgot-password {
  color: #007bff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #0056b3;
}
</style>