<template>
  <div class="upload-container">    <div class="file-upload-wrapper">
      <div class="file-input-container">
        <input type="file" ref="fileInput" id="file-upload" @change="onFileSelect" />
        <label for="file-upload" class="file-input-label">
          <span>选择文件</span>
        </label>
      </div>
      <div v-if="selectedFileName" class="selected-file">
        <span class="file-name">{{ selectedFileName }}</span>
      </div>
      <button
        @click="startUpload"
        class="upload-button"
        :disabled="!selectedFileName"
      >
        上传
      </button>    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Cookies from 'js-cookie'

// Define emits
const emit = defineEmits(['upload-start', 'upload-progress', 'upload-complete', 'upload-error'])

// 常量：切片大小
const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB

// 文件输入和进度状态
const fileInput = ref(null)
const progress = ref(-1)
const selectedFileName = ref('')

// 文件选择处理
function onFileSelect(event) {
  const file = event.target.files[0]
  selectedFileName.value = file ? file.name : ''
}

// 取 token
function getToken() {
  return `${Cookies.get('token')}`
}

async function startUpload() {
  const file = fileInput.value.files[0]
  if (!file) return alert('请选择文件')

  const token = getToken()
  if (!token) return alert('未登录或 token 丢失')

  const fileId = `${file.name}-${file.size}-${file.lastModified}`

  // Emit upload start event
  emit('upload-start', { fileName: file.name, fileSize: file.size })

  // 1. 查询已上传分片
  let uploaded = []
  try {
    const res = await fetch(
      `${import.meta.env.VITE_API_BASE}/upload1/status?fileId=${encodeURIComponent(fileId)}`,
      {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
      }
    )
    if (res.ok) {
      uploaded = await res.json()
    } else if (res.status === 401) {
      emit('upload-error', '身份验证失败，请重新登录')
      return alert('身份验证失败，请重新登录')
    }
  } catch (e) {
    console.warn('获取已上传分片失败，开始全量上传', e)
  }

  const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
  let uploadedCount = uploaded.length
  progress.value = uploadedCount / totalChunks
  
  // Emit initial progress
  emit('upload-progress', Math.floor(progress.value * 100))

  // 2. 分片上传
  for (let idx = 0; idx < totalChunks; idx++) {
    if (uploaded.includes(idx)) continue

    const start = idx * CHUNK_SIZE
    const chunk = file.slice(start, start + CHUNK_SIZE)
    const form = new FormData()
    form.append('chunk', chunk)
    form.append('fileId', fileId)
    form.append('chunkIndex', idx)
    form.append('totalChunks', totalChunks)
    form.append('filename', file.name)

    let attempts = 0
    while (attempts < 3) {
      try {
        const r = await fetch(
          `${import.meta.env.VITE_API_BASE}/upload1/chunk`,
          {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` },
            body: form,
          }
        )
        if (!r.ok) {
          if (r.status === 401) {
            emit('upload-error', '身份验证失败，请重新登录')
            return alert('身份验证失败，请重新登录')
          }
          throw new Error(`状态 ${r.status}`)
        }
        uploadedCount++
        progress.value = uploadedCount / totalChunks
        
        // Emit progress update
        emit('upload-progress', Math.floor(progress.value * 100))
        break
      } catch (e) {
        attempts++
        console.warn(`第 ${idx} 片第 ${attempts} 次上传失败，重试...`, e)
        if (attempts === 3) {
          emit('upload-error', `第 ${idx} 片上传失败，终止上传`)
          return alert(`第 ${idx} 片上传失败，终止上传`)
        }
      }
    }
  }

  // 3. 通知后端合并，并在完成后调用单文件上传接口
  try {
    const mergeRes = await fetch(
      `${import.meta.env.VITE_API_BASE}/upload1/merge?fileId=${encodeURIComponent(
        fileId
      )}&filename=${encodeURIComponent(file.name)}`,
      {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      }
    )
    if (!mergeRes.ok) {
      if (mergeRes.status === 401) {
        emit('upload-error', '身份验证失败，请重新登录')
        alert('身份验证失败，请重新登录')
      } else {
        emit('upload-error', '合并失败，状态码：' + mergeRes.status)
        throw new Error('合并失败，状态码：' + mergeRes.status)
      }
      progress.value = -1
      return
    }

    // 显示 100%，然后短暂停留，再执行下一步
    progress.value = 1
    emit('upload-progress', 100)
    
    setTimeout(async () => {
      // 关闭进度弹窗
      progress.value = -1

      // 4. 调用单文件上传接口，上传元数据及文件
      // 根据文件后缀映射 FileType 枚举
      const ext = file.name.split('.').pop().toLowerCase()
      let fileType = 'OTHER'
      const imageExts    = ['jpg','jpeg','png','gif','bmp','webp']
      const videoExts    = ['mp4','mov','avi','mkv','flv','webm']
      const audioExts    = ['mp3','wav','ogg','flac']
      const docExts      = ['pdf','doc','docx','xls','xlsx','ppt','pptx','txt','csv','md']

      if (imageExts.includes(ext)) fileType = 'IMAGE'
      else if (videoExts.includes(ext)) fileType = 'VIDEO'
      else if (audioExts.includes(ext)) fileType = 'AUDIO'
      else if (docExts.includes(ext))   fileType = 'DOCUMENT'

      const metaForm = new FormData()
      metaForm.append('file', file)
      metaForm.append('file_name', file.name)
      metaForm.append('file_type', fileType)
      metaForm.append('file_size', file.size)

      try {
        const uploadRes = await fetch(
          `${import.meta.env.VITE_API_BASE}/upload1/upload`,
          {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` },
            body: metaForm,
          }
        )
        if (uploadRes.ok) {
          const result = await uploadRes.json()
          emit('upload-complete', { fileId: result.id, fileName: file.name })
          alert('上传完成，文件ID：' + result.id)
          
          // Reset form
          selectedFileName.value = ''
          fileInput.value.value = ''
        } else if (uploadRes.status === 401) {
          emit('upload-error', '元数据上传认证失败，请重新登录')
          alert('元数据上传认证失败，请重新登录')
        } else {
          emit('upload-error', '元数据上传失败，状态码：' + uploadRes.status)
          throw new Error('元数据上传失败，状态码：' + uploadRes.status)
        }
      } catch (err) {
        console.error(err)
        emit('upload-error', '元数据上传失败，请稍后重试')
        alert('元数据上传失败，请稍后重试')
      }
    }, 500)
  } catch (err) {
    console.error(err)
    emit('upload-error', '合并失败，请稍后重试')
    alert('合并失败，请稍后重试')
    progress.value = -1
  }
}

</script>

<style scoped>
/* Upload Container */
.upload-container {
  margin: 20px 0;
}

.file-upload-wrapper {
  display: flex;
  gap: 15px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

/* File Input Styling */
.file-input-container {
  position: relative;
}

#file-upload {
  position: absolute;
  opacity: 0;
  width: 1px;
  height: 1px;
  overflow: hidden;
}

.file-input-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
  min-width: 120px;
  justify-content: center;
}

.file-input-label:hover {
  background: #2563eb;
}

.file-input-label:active {
  background: #1d4ed8;
}

/* Selected File Display */
.selected-file {
  max-width: 300px;
  padding: 8px 12px;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.file-name {
  font-size: 14px;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

/* Upload Button */
.upload-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  min-width: 80px;
  justify-content: center;
}

.upload-button:hover:not(:disabled) {
  background: #2563eb;
}

.upload-button:active:not(:disabled) {
  background: #1d4ed8;
}

.upload-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #9ca3af;
}

/* Icons */
.upload-icon {
  font-size: 16px;
  display: inline-block;
}

/* Responsive Design */
@media (max-width: 480px) {
  .file-upload-wrapper {
    flex-direction: column;
    gap: 10px;
  }
  
  .file-input-label,
  .upload-button {
    width: 100%;
    max-width: 200px;
  }
  
  .selected-file {
    width: 100%;
    max-width: 200px;
  }
  
  .modal-content {
    width: 90%;
    max-width: 300px;
    padding: 20px;
  }
}
</style>
