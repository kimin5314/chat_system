<template>
  <div class="change-password-container">
    <h2>修改密码</h2>
    <div class="form-group">
      <label>原始密码：</label>
      <input type="password" v-model="userPassword" />
    </div>
    <div class="form-group">
      <label>新密码：</label>
      <input type="password" v-model="newPassword" />
    </div>
    <div class="form-group">
      <label>确认新密码：</label>
      <input type="password" v-model="confirmPassword" />
    </div>
    <div class="button-group">
      <button @click="handleChangePassword">提交修改</button>
    </div>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="successMessage" class="success">{{ successMessage }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Cookies from "js-cookie";
import request from '@/utils/request';

// 表单字段
const userPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');

// 提示信息
const errorMessage = ref('');
const successMessage = ref('');

const handleChangePassword = async () => {
  errorMessage.value = '';
  successMessage.value = '';

  // 表单验证
  if (!userPassword.value || !newPassword.value || !confirmPassword.value) {
    errorMessage.value = '请填写所有字段';
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = '两次输入的新密码不一致';
    return;
  }
  const isLoading = ref(false);

  if (isLoading.value) return; // 防止重复提交

  isLoading.value = true;
  try {    const response = await request.post(
        '/user/password',
        {
          userPassword: userPassword.value,
          newPassword: newPassword.value,
        },
        {
          headers: {
            Authorization: `Bearer ${Cookies.get('token')}`,
          },
        }
    );
    if (response.data && response.data.code === '200') {
      successMessage.value = '密码修改成功！';
      userPassword.value = '';
      newPassword.value = '';
      confirmPassword.value = '';
    } else {
      errorMessage.value = response.data?.msg || '密码修改失败';
    }
  } catch (err) {
    console.error('Error from server:', err); // 打印错误信息
    errorMessage.value = err.response?.data?.msg || '密码修改失败';
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.change-password-container {
  max-width: 450px;
  margin: var(--spacing-4xl) auto;
  padding: var(--spacing-3xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-medium);
  border: 1px solid var(--border-light);
  animation: slideUp 0.5s ease;
}

h2 {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
  color: var(--text-primary);
  font-size: var(--font-size-2xl);
  font-weight: 600;
}

.form-group {
  margin-bottom: var(--spacing-lg);
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: var(--spacing-xs);
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

input[type="password"] {
  padding: var(--spacing-md);
  border-radius: var(--border-radius-small);
  border: 1px solid var(--border-color);
  font-size: var(--font-size-base);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

input[type="password"]:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.button-group {
  text-align: center;
  margin-top: var(--spacing-xl);
}

button {
  padding: var(--spacing-md) var(--spacing-2xl);
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

button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

button:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.error {
  color: #dc3545;
  text-align: center;
  margin-top: 10px;
}

.success {
  color: #28a745;
  text-align: center;
  margin-top: 10px;
}
</style>