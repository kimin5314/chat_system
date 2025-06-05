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
  import axios from 'axios'
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
      const response = await axios.get(`${import.meta.env.VITE_API_BASE}/file/demo`, {
        responseType: 'blob',
        headers: { Authorization: `Bearer ${token}` },
        params: { fileId }
      })
      console.log(response)
      // 4. 检查 content-type
      const contentType = response.headers['content-type'] || ''
      console.log(contentType)
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
      errorMsg.value = err.message || '无法加载该文件，请稍后重试'
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
  .preview-page { padding: 16px; }
  .btn-back { background: transparent; border: none; color: #3b82f6; cursor: pointer; }
  .btn-back:hover { text-decoration: underline; }
  .loading, .error { text-align: center; margin-top: 40px; color: #555; }
  .preview-container { display: flex; justify-content: center; align-items: center; margin-top: 24px; }
  /* 图片 */
  .preview-content img { max-width: 90vw; max-height: 80vh; border-radius: 4px; }
  /* PDF iframe */
  .iframe-preview { width: 90vw; height: 80vh; }
  /* 文本 */
  .text-preview {
    background: #f7f7f7; padding: 12px; border-radius: 4px;
    max-width: 90vw; max-height: 80vh; overflow-y: auto;
    white-space: pre-wrap; word-break: break-all;
  }
  /* 下载链接 */
  .btn-download {
    display: inline-block; margin-top: 16px; padding: 8px 12px;
    background: #3b82f6; color: #fff; border-radius: 4px; text-decoration: none;
  }
  .btn-download:hover { background: #2563eb; }
  </style>
  
  <style scoped>
  .preview-page {
    padding: 16px;
  }
  
  .btn-back {
    background: transparent;
    border: none;
    color: #3b82f6;
    cursor: pointer;
    font-size: 14px;
    margin-bottom: 12px;
  }
  .btn-back:hover {
    text-decoration: underline;
  }
  
  .loading, .error {
    text-align: center;
    color: #555;
    margin-top: 40px;
  }
  
  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  /* 图片预览 */
  .preview-content img {
    max-width: 90vw;
    max-height: 80vh;
    border-radius: 4px;
  }
  
  /* PDF 内嵌预览：宽高可根据需要调整 */
  .iframe-preview {
    width: 90vw;
    height: 80vh;
  }
  
  /* 文本预览 */
  .text-preview {
    background: #f7f7f7;
    padding: 12px;
    border-radius: 4px;
    max-width: 90vw;
    max-height: 80vh;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }
  
  /* 下载链接按钮 */
  .btn-download {
    display: inline-block;
    margin-top: 16px;
    padding: 8px 12px;
    background: #3b82f6;
    color: #fff;
    border-radius: 4px;
    text-decoration: none;
  }
  .btn-download:hover {
    background: #2563eb;
  }
  </style>
  