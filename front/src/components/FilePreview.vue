<template>
    <div class="preview-page">
      <!-- <button class="btn-back" @click="goBack">← 返回列表</button> -->
  
      <div v-if="loading" class="loading">正在加载文件...</div>
      <div v-else-if="errorMsg" class="error">{{ errorMsg }}</div>
      <div v-else class="preview-container">
        <!-- 图片预览 -->
        <div v-if="previewType.startsWith('image/')" class="preview-content">
          <img :src="previewUrl" alt="图片预览" />
        </div>
        <!-- PDF 预览 -->
        <div v-else-if="previewType === 'application/pdf'" class="preview-content">
          <iframe :src="previewUrl" frameborder="0" class="iframe-preview"></iframe>
        </div>
        <!-- 文本预览 -->
        <div v-else-if="previewType.startsWith('text/')" class="preview-content">
          <pre class="text-preview">{{ textContent }}</pre>
        </div>
        <!-- 其他类型，提供下载 -->
        <div v-else class="preview-content">
          <p>暂不支持在线预览此类型文件（{{ previewType }}）。</p>
          <a :href="previewUrl" target="_blank" class="btn-download">点击下载查看</a>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import request from '@/utils/request'
  import Cookies from 'js-cookie'
  
  const route = useRoute()
  const router = useRouter()
  // 强校验 fileId 是否存在且为数字
  const fileId = Number(route.params.fileId)
  
  const loading = ref(true)
  const errorMsg = ref('')
  const previewType = ref('')
  const previewUrl = ref('')
  const textContent = ref('')
    onMounted(async () => {
    // 1. 验证 fileId
    if (!fileId || isNaN(fileId)) {
      errorMsg.value = '文件 ID 无效，请返回列表后重新选择'
      loading.value = false
      return
    }
  
    // 2. 验证 token
    const token = Cookies.get('token')
    if (!token) {
      errorMsg.value = '未检测到登录状态，请先登录'
      loading.value = false
      return
    }
  
    try {
      // 3. 发送请求
      const response = await request.get('/file/demo', {
        responseType: 'blob',
        headers: { Authorization: `Bearer ${token}` },
        params: { fileId }
      })
      
      console.log('Response:', response)
      
      // 4. 检查 content-type
      const contentType = response.headers['content-type'] || ''
      console.log('Content-Type:', contentType)
      
      // 若后端以 JSON/HTML 返回错误页面，则抛出异常
      if (
        contentType.startsWith('application/json') ||
        contentType.includes('text/html')
      ) {
        const errText = await new Blob([response.data]).text()
        try {
          const errJson = JSON.parse(errText) // 典型 { code:"400", msg:"xxx" }
          throw new Error(errJson.msg || '后端返回未知错误')
        } catch {
          throw new Error(errText)
        }
      }
  
      // 5. 真正的文件流
      previewType.value = contentType
      const blob = new Blob([response.data], { type: contentType })
      previewUrl.value = URL.createObjectURL(blob)
  
      if (contentType.startsWith('text/')) {
        textContent.value = await blob.text()
      }
      loading.value = false
    } catch (err) {
      console.error('文件预览出错：', err)
      
      let errorMessage = '无法加载该文件，请稍后重试'
      
      if (err.response) {
        if (err.response.data instanceof Blob) {
          // If the error response is also a blob, try to read it as text
          try {
            const errorText = await err.response.data.text();
            const errorJson = JSON.parse(errorText);
            errorMessage = errorJson.msg || errorJson.message || errorText;
          } catch {
            errorMessage = '服务器返回了错误的响应格式';
          }
        } else {
          errorMessage = err.response.data?.msg || err.response.data?.message || err.message;
        }
      } else {
        errorMessage = err.message;
      }
      
      errorMsg.value = errorMessage
      loading.value = false
    }
  })
    onUnmounted(() => {
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
    }
  })
  </script>
  
  <style scoped>
  .preview-page {
    padding: var(--spacing-2xl);
    background: var(--bg-primary);
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-medium);
    border: 1px solid var(--border-light);
    animation: slideUp 0.5s ease;
    margin: var(--spacing-xl);
    min-height: 80vh;
  }

  .btn-back {
    background: transparent;
    border: none;
    color: var(--primary-color);
    cursor: pointer;
    font-size: var(--font-sm);
    font-weight: var(--font-medium);
    margin-bottom: var(--spacing-lg);
    padding: var(--spacing-sm) var(--spacing-md);
    border-radius: var(--border-radius-small);
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-xs);
  }

  .btn-back:hover {
    background: var(--bg-hover);
    color: var(--primary-hover);
    transform: translateX(-2px);
  }

  .loading, .error {
    text-align: center;
    color: var(--text-secondary);
    margin-top: var(--spacing-3xl);
    font-size: var(--font-md);
    font-weight: var(--font-medium);
  }

  .error {
    color: var(--danger-color);
    background: var(--danger-color-alpha);
    padding: var(--spacing-lg);
    border-radius: var(--border-radius-small);
    border: 1px solid var(--danger-color);
  }

  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: var(--spacing-lg);
    background: var(--bg-secondary);
    border-radius: var(--border-radius);
    border: 1px solid var(--border-light);
    min-height: 60vh;
  }

  .preview-content {
    max-width: 100%;
    text-align: center;
  }

  /* Image preview */
  .preview-content img {
    max-width: 90vw;
    max-height: 70vh;
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-medium);
    transition: transform 0.3s ease;
  }

  .preview-content img:hover {
    transform: scale(1.02);
  }

  /* PDF embedded preview */
  .iframe-preview {
    width: 90vw;
    height: 70vh;
    border: none;
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-medium);
  }

  /* Text preview */
  .text-preview {
    background: var(--bg-primary);
    border: 1px solid var(--border-light);
    padding: var(--spacing-xl);
    border-radius: var(--border-radius);
    max-width: 90vw;
    max-height: 60vh;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: 'Courier New', monospace;
    font-size: var(--font-sm);
    line-height: 1.6;
    color: var(--text-primary);
    text-align: left;
  }

  /* Download link button */
  .btn-download {
    display: inline-block;
    margin-top: var(--spacing-lg);
    padding: var(--spacing-md) var(--spacing-xl);
    background: var(--primary-gradient);
    color: white;
    border-radius: var(--border-radius);
    text-decoration: none;
    font-weight: var(--font-medium);
    font-size: var(--font-sm);
    transition: all 0.3s ease;
    box-shadow: var(--shadow-light);
  }

  .btn-download:hover {
    background: var(--primary-hover);
    transform: translateY(-2px);
    box-shadow: var(--shadow-medium);
  }

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @media (max-width: 768px) {
    .preview-page {
      margin: var(--spacing-md);
      padding: var(--spacing-lg);
    }
    
    .preview-content img,
    .iframe-preview,
    .text-preview {
      max-width: 95vw;
      max-height: 50vh;
    }
    
    .preview-container {
      padding: var(--spacing-md);
    }  }
  </style>
  