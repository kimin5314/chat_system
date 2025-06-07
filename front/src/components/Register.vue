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
import request from '@/utils/request'

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
    const response = await request.post('/user/register', {
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
  background: var(--bg-light);
  padding: var(--spacing-lg);
}

.register-box {
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

.register-button {
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

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.register-button:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.back-link {
  display: block;
  text-align: center;
  margin-top: var(--spacing-lg);
  color: var(--primary-color);
  text-decoration: none;
  font-size: var(--font-size-sm);
  font-weight: 500;
  transition: all var(--transition-fast);
  padding: var(--spacing-xs);
  border-radius: var(--border-radius-small);
}

.back-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
  transform: translateY(-1px);
}

@media (max-width: 480px) {
  .register-box {
    padding: var(--spacing-2xl);
  }
}
</style>
