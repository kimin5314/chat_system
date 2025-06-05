<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册新账号</h2>

      <div class="input-group">
        <label for="username">用户名：</label>
        <input type="text" id="username" v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
      </div>

      <div class="input-group">
        <label for="password">密码：</label>
        <input type="password" id="password" v-model="form.password" placeholder="请输入密码" autocomplete="new-password" />
      </div>

      <div class="input-group">
        <label for="confirmPassword">确认密码：</label>
        <input type="password" id="confirmPassword" v-model="form.confirmPassword" placeholder="请再次输入密码" autocomplete="new-password" />
      </div>

      <div class="input-group">
        <label for="displayName">昵称：</label>
        <input type="text" id="displayName" v-model="form.displayName" placeholder="请输入昵称" />
      </div>

      <button class="register-button" @click="register" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <router-link to="/login" class="back-link">返回登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  displayName: ''
})

const loading = ref(false)

const register = async () => {
  if (loading.value) return
  if (!form.value.username || !form.value.password || !form.value.confirmPassword || !form.value.displayName) {
    alert('请填写所有字段')
    return
  }

  if (form.value.password.length < 6 || form.value.password.length > 16) {
    alert('密码需在6 - 16位之间')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    alert('两次密码不一致')
    return
  }

  loading.value = true

  try {
    const response = await axios.post(`${import.meta.env.VITE_API_BASE}/user/register`, {
      username: form.value.username,
      password: form.value.password,
      displayName: form.value.displayName
    })

    if (response.data.code === '200') {
      alert('注册成功，请登录！')
      router.push('/login')
    } else {
      alert(response.data.msg || '注册失败')
    }
  } catch (err) {
    console.error(err)
    alert('请求失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f4;
}

.register-box {
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

.register-button {
  width: 100%;
  padding: 12px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.register-button:hover {
  background-color: #218838;
}

.register-button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

.back-link {
  display: block;
  text-align: center;
  margin-top: 15px;
  color: #007bff;
  text-decoration: none;
}

.back-link:hover {
  color: #0056b3;
}
</style>
