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
import axios from "axios";

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
  try {
    const response = await axios.post(
        `${import.meta.env.VITE_API_BASE}/user/password`,
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
  max-width: 400px;
  margin: 40px auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 6px;
  font-weight: bold;
}

input[type="password"] {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.button-group {
  text-align: center;
}

button {
  padding: 10px 24px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
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