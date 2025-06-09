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
        </div>        <div class="info-item">
          <label>注册时间：</label>
          <input :value="createdAt" readonly class="readonly" />
        </div>
      </div>
    </div>
    
    <!-- E2EE Settings Section -->
    <div class="e2ee-section">
      <div class="section-header">
        <h2>端到端加密设置</h2>
        <p>管理您的聊天加密功能</p>
      </div>
      <E2EESettings />
    </div>
  </div>
</template>

<script setup>
import { ref , onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import E2EESettings from '@/components/E2EESettings.vue';
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
      const response = await request.post('/user/profile', {}, {
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
  await request.post('/user/update',{
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
    const response = await request.post('/user/upload-avatar', formData, {
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
  max-width: 800px;
  margin: var(--spacing-4xl) auto;
  padding: var(--spacing-4xl);
  background: var(--bg-primary);
  box-shadow: var(--shadow-medium);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
  min-height: 400px;
  animation: slideUp 0.5s ease;
}

.profile-header {
  display: flex;
  justify-content: space-between;  align-items: center;
  flex-wrap: wrap;
  margin-bottom: var(--spacing-3xl);
  padding-bottom: var(--spacing-2xl);
  border-bottom: 1px solid var(--border-light);
}

.profile-header h1 {
  font-size: var(--font-size-2xl);
  margin: 0;
  color: var(--text-primary);
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: var(--spacing-md);
}

.button-group button {
  padding: var(--spacing-sm) var(--spacing-lg);
  background: var(--primary-gradient);
  color: white;
  border: none;
  border-radius: var(--border-radius-small);
  cursor: pointer;
  font-weight: 600;
  font-size: var(--font-size-base);
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.button-group button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.logout-button {
  background: linear-gradient(135deg, #dc3545, #c82333) !important;
}

.logout-button:hover {
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4) !important;
}

.profile-content {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-3xl);
  align-items: flex-start;
}

.profile-avatar {
  flex-shrink: 0;
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
}

.profile-avatar img {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--border-light);
  box-shadow: var(--shadow-light);
  transition: all var(--transition-medium);
}

.profile-avatar img:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-medium);
}

.avatar-upload {
  margin-top: var(--spacing-lg);
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
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--secondary-color);
  color: white;
  border-radius: var(--border-radius-small);
  cursor: pointer;
  font-size: var(--font-size-base);
  font-weight: 600;
  transition: all var(--transition-fast);
  border: none;
  user-select: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.file-input-label:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.file-input-label:active {
  background: #1d4ed8;
}

.upload-icon {
  font-size: var(--font-size-md);
}

.profile-info {
  flex: 1;
  min-width: 300px;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
}

.info-item {
  display: flex;
  align-items: center;
  font-size: var(--font-size-md);
  width: 100%;
  margin-bottom: var(--spacing-lg);
}

.info-item label {
  width: 120px;
  padding: var(--spacing-sm);
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.info-item input {
  flex: 1;
  font-size: var(--font-size-md);
  padding: var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-small);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all var(--transition-fast);}

.info-item input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.info-item input.readonly {
  border: none;
  background: transparent;
  color: var(--text-primary);
  padding: var(--spacing-sm);
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-container {
    margin: var(--spacing-lg);
    padding: var(--spacing-2xl);
    min-height: auto;
  }
  
  .profile-header {
    flex-direction: column;
    gap: var(--spacing-lg);
    text-align: center;
  }
  
  .profile-content {
    flex-direction: column;
    gap: var(--spacing-xl);
  }
  
  .profile-info {
    min-width: auto;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
    .info-item label {
    width: auto;
    margin-bottom: var(--spacing-xs);
  }
}

.e2ee-section {
  margin-top: var(--spacing-3xl);
  padding: var(--spacing-2xl);
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-light);
}

.section-header {
  margin-bottom: var(--spacing-xl);
}

.section-header h2 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
}

.section-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}
</style>
