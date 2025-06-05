<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人信息</h1>
      <div class="button-group">
        <button v-if="!isEditing" @click="startEditing">修改信息</button>
        <button v-if="isEditing" @click="saveChanges">保存修改</button>
        <button @click="logout" class="logout-button">退出登录</button>
      </div>
    </div>

    <div class="profile-content">      <div class="profile-avatar">
        <img :src="avatarUrl" alt="用户头像" style="background-color: lightgray; border: 2px solid black;"/>
        <div v-if="isEditing" class="avatar-upload">
          <div class="file-input-wrapper">
            <input type="file" id="avatar-input" @change="handleAvatarChange" accept="image/*" />            <label for="avatar-input" class="file-input-label">
              <span>选择头像</span>
            </label>
          </div>
        </div>
      </div>

      <div class="profile-info">
        <div class="info-item">
          <label>用户名：</label>
          <input :value="username" readonly class="readonly" />
        </div>
        <div class="info-item">
          <label>昵称：</label>
          <input
              v-model="displayName"
              :class="{ readonly: !isEditing }"
          />
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <input :value="createdAt" readonly class="readonly" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref , onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from "axios";
import Cookies from "js-cookie";

const router = useRouter();

const avatarUrl = ref('');
const username = ref('');
const displayName = ref('');
const createdAt = ref('');
const isEditing = ref(false);
const newAvatar=ref(null);

onMounted (async () => {
  const token = sessionStorage.getItem('token') || Cookies.get('token');
  if (token) {
    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE}/user/profile`, {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const user = response.data.data;
      // Handle avatar URL - if it starts with /uploads, prepend the API base URL
      if (user.avatarUrl && user.avatarUrl.startsWith('/uploads')) {
        avatarUrl.value = `${import.meta.env.VITE_API_BASE}${user.avatarUrl}`;
      } else {
        avatarUrl.value = user.avatarUrl || 'https://avatars.githubusercontent.com/u/583231?v=4';
      }
      username.value = user.username;
      displayName.value = user.displayName;
      createdAt.value = user.createdAt;
    } catch (error) {
      console.error('获取个人信息失败:', error);
    }
  }
});


const startEditing = () => {
  isEditing.value = true;
};

const saveChanges = async() => {
  try{
    const token = sessionStorage.getItem('token') || Cookies.get('token');
    if(token){
      // 如果有新头像，先上传头像
      if (newAvatar.value) {
        await uploadAvatar();
      }
      
      // 更新其他信息
      await update(displayName.value);
      isEditing.value = false;
      alert("信息修改成功");
    }
  }catch(error){
    console.error("保存修改失败",error);
    alert("保存修改失败: " + error.message);
  }
};

const update = async (newDisplayName) => {
  const token = sessionStorage.getItem('token') || Cookies.get('token');
  await axios.post(`${import.meta.env.VITE_API_BASE}/user/update`,{
    displayName: newDisplayName
  },{
    headers:{
        Authorization: `Bearer ${token}`
    }
  })
}
const logout = () => {
  // Clear all authentication data
  Cookies.remove('token');
  sessionStorage.removeItem('token');
  localStorage.removeItem('token');
  
  router.push('/login');
};

const handleAvatarChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件');
      return;
    }
    
    // 验证文件大小 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('文件大小不能超过5MB');
      return;
    }
    
    newAvatar.value = file;
    
    // 预览图片
    const reader = new FileReader();
    reader.onload = (e) => {
      avatarUrl.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

const uploadAvatar = async () => {
  if (!newAvatar.value) return;
  
  const token = sessionStorage.getItem('token') || Cookies.get('token');
  const file = newAvatar.value;
    // Create FormData for file upload
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    // Upload avatar using a simple endpoint
    const response = await axios.post(`${import.meta.env.VITE_API_BASE}/user/upload-avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    });
      if (response.data.code === "200") {
      // Update avatar URL to the direct file URL with API base
      avatarUrl.value = `${import.meta.env.VITE_API_BASE}${response.data.data.avatarUrl}`;
      newAvatar.value = null;
    } else {
      throw new Error(response.data.msg || '头像上传失败');
    }
  } catch (err) {
    console.error(err);
    throw new Error('头像上传失败：' + (err.response?.data?.message || err.message));
  }
};
</script>

<style scoped>
.profile-container {
  max-width: 700px;
  margin: 50px auto;
  padding: 30px;
  background-color: #fafafa;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-family: Arial, sans-serif;
  height: 300px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.profile-header h1 {
  font-size: 24px;
  margin: 0;
}

.button-group {
  display: flex;
  gap: 10px;
}

.button-group button {
  padding: 8px 14px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.button-group button:hover {
  background-color: #0056b3;
}

.logout-button {
  background-color: #dc3545;
}

.logout-button:hover {
  background-color: #c82333;
}

.profile-content {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  align-items: flex-start;
}

.profile-avatar {
  flex-shrink: 0;
  text-align: center;
}

.profile-avatar img {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #ddd;
}

.avatar-upload {
  margin-top: 10px;
  text-align: center;
}

.file-input-wrapper {
  position: relative;
  display: inline-block;
}

.file-input-wrapper input[type="file"] {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.file-input-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  user-select: none;
}

.file-input-label:hover {
  background: #2563eb;
}

.file-input-label:active {
  background: #1d4ed8;
}

.upload-icon {
  font-size: 16px;
}

.profile-info {
  flex: 1;
  min-width: 240px;
  margin-top: 10px;
  margin-left: 30px;
}

.info-item {
  display: flex;
  font-size: 20px;
  width: 100%;
}

.info-item label {
  width: 100px;
  padding: 8px;
  font-weight: bold;
  color: #333;
}

.info-item input {
  flex: 1;
  font-size: 20px;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 6px;
  width: 100%;
}

.info-item input.readonly {
  border: none;
  background-color: transparent;
  color: #333;
  padding: 8px;
}
</style>
