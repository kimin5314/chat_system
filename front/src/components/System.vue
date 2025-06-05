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
  background: var(--bg-light);
  padding: var(--spacing-3xl);
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4xl);
  padding: var(--spacing-xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
}

.logo h1 {
  margin: 0;
  font-size: var(--font-size-2xl);
  color: var(--primary-color);
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-buttons {
  display: flex;
  gap: var(--spacing-md);
}

.nav-button {
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--primary-gradient);
  color: white;
  text-decoration: none;
  border-radius: var(--border-radius-small);
  font-weight: 600;
  font-size: var(--font-size-base);
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.nav-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.welcome-card {
  width: 100%;
  max-width: 700px;
  padding: var(--spacing-4xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-medium);
  text-align: center;
  animation: slideUp 1s ease;
  border: 1px solid var(--border-light);
  backdrop-filter: blur(10px);
}

.welcome-card h1 {
  font-size: var(--font-size-4xl);
  margin-bottom: var(--spacing-xl);
  color: var(--text-primary);
  font-weight: 700;
}

.welcome-card p {
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-3xl);
  line-height: 1.6;
}

.icon-container {
  display: flex;
  justify-content: center;
  gap: var(--spacing-3xl);
  margin: var(--spacing-2xl) 0;
}

.icon-container i {
  font-size: 48px;
  color: var(--primary-color);
  transition: all var(--transition-medium);
  padding: var(--spacing-lg);
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.1);
}

.icon-container i:hover {
  transform: translateY(-8px) scale(1.1);
  background: var(--primary-gradient);
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-2xl);
  width: 100%;
  margin-top: var(--spacing-4xl);
}

.feature-card {
  padding: var(--spacing-3xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
  text-align: center;
  transition: all var(--transition-medium);
  border: 1px solid var(--border-light);
}

.feature-card i {
  font-size: 64px;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xl);
  transition: all var(--transition-medium);
}

.feature-card h3 {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
  font-weight: 600;
}

.feature-card p {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  line-height: 1.6;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-heavy);
}

.feature-card:hover i {
  color: var(--secondary-color);
  transform: scale(1.1);
}

@media (max-width: 768px) {
  .home-container {
    padding: var(--spacing-lg);
  }
  
  .header {
    flex-direction: column;
    gap: var(--spacing-lg);
    text-align: center;
  }
  
  .welcome-card {
    padding: var(--spacing-2xl);
  }
  
  .welcome-card h1 {
    font-size: var(--font-size-3xl);
  }
  
  .feature-cards {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .icon-container {
    flex-wrap: wrap;
    gap: var(--spacing-lg);
  }
}
</style>