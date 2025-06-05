<template>
  <div class="home-container">
    <div class="header">
      <div class="logo">
        <h1>平台名称</h1>
      </div>
      <div class="nav-buttons">
        <router-link to="/login" class="nav-button">登录</router-link>
        <router-link to="/register" class="nav-button">注册</router-link>
      </div>
    </div>
    <div class="welcome-card">
      <h1>欢迎来到我们的聊天平台</h1>
      <p>在这里，你可以尽情探索各种有趣的功能和内容。</p>
      <div class="icon-container">
        <i class="fas fa-rocket"></i>
        <i class="fas fa-users"></i>
        <i class="fas fa-cogs"></i>
      </div>
    </div>
    <div class="feature-cards">
      <div class="feature-card">
        <i class="fas fa-heart"></i>
        <h3>好友互动</h3>
        <p>与好友保持联系，分享生活点滴。</p>
      </div>
      <div class="feature-card">
        <i class="fas fa-search"></i>
        <h3>快速搜索</h3>
        <p>轻松找到你想要的信息和资源。</p>
      </div>
      <div class="feature-card">
        <i class="fas fa-tools"></i>
        <h3>个性化设置</h3>
        <p>定制属于你的独特体验。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 这里暂时没有复杂的逻辑，如有需要可添加数据获取等操作
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 95vh;
  background-color: #f0f5ff;
  padding: 30px;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 80px;
  background-color: #f0f5ff;
}

.logo h1 {
  margin-left: 30px;
  font-size: 24px;
  color: #007bff;
  background-color: #f0f5ff;
  width: auto;
}

.nav-buttons {
  display: flex;
}

.nav-button {
  margin-left: 15px;
  padding: 10px 20px;
  background-color: #007bff;
  color: #fff;
  text-decoration: none;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}

.nav-button:hover {
  background-color: #0056b3;
}

.welcome-card {
  width: 600px;
  padding: 40px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  text-align: center;
  animation: fadeInUp 1s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-card h1 {
  font-size: 36px;
  margin-bottom: 20px;
  color: #333;
}

.welcome-card p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.icon-container {
  display: flex;
  justify-content: center;
}

.icon-container i {
  font-size: 48px;
  color: #007bff;
  margin: 0 20px;
  transition: transform 0.3s ease;
}

.icon-container i:hover {
  transform: translateY(-5px);
}

.feature-cards {
  display: flex;
  justify-content: space-around;
  width: 100%;
  margin-top: 40px;
}

.feature-card {
  width: 300px;
  padding: 30px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card i {
  font-size: 64px;
  color: #007bff;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
}

.feature-card p {
  font-size: 16px;
  color: #666;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
}
</style>