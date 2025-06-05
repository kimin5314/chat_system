<template>
  <div class="file-manager">
    <div class="header">
      <input
        v-model="search"
        @keyup.enter="fetchFiles"
        placeholder="搜索文件"
        class="search-input"
      />
    </div>


<table class="file-table">
  <thead>
    <tr>
      <th @click="sortBy('fileName')">文件名</th>
      <th @click="sortBy('fileType')">类型</th>
      <th @click="sortBy('fileSize')">大小</th>
      <th @click="sortBy('uploadedAt')">上传时间</th>
      <th>操作</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="file in pagedFiles" :key="file.id">
      <td>{{ file.fileName }}</td>
      <td>{{ file.fileType }}</td>
      <td>{{ formatSize(file.fileSize) }}</td>
      <td>{{ formatDate(file.uploadedAt) }}</td>
      <td class="actions">
        <button @click="download(file.id)">下载</button>
        <button @click="previewFile(file.id)">预览</button>
        <button @click="remove(file.id)">删除</button>
        <button @click="share(file.id)">分享</button>
      </td>
    </tr>
    <tr v-if="!filteredFiles.length">
      <td colspan="5" class="no-data">暂无文件</td>
    </tr>
  </tbody>
</table>

<div class="footer">
  <span>共 {{ files.length }} 个文件</span>

  <!-- 分页条 -->
  <div class="pagination">
    <button
      :disabled="currentPage === 1"
      @click="currentPage--"
    >&laquo; 上一页</button>

    <button
      v-for="page in displayPages"
      :key="page + '_' + String(Math.random())"
      :class="{ active: page === currentPage }"
      :disabled="page === '...'"
      @click="page !== '...' && (currentPage = page)"
    >
      {{ page }}
    </button>

    <button
      :disabled="currentPage === totalPages"
      @click="currentPage++"
    >下一页 &raquo;</button>
  </div>
  <!-- <input type="file" @change="onFileSelect" />
  <button class="upload-button" @click="upload">上传</button> -->
  <UploadFile 
    @upload-start="handleUploadStart"
    @upload-progress="handleUploadProgress"
    @upload-complete="handleUploadComplete"
    @upload-error="handleUploadError"
  />
  
  <!-- Upload Progress Display -->
  <div v-if="uploadStatus.isUploading" class="upload-progress-container">
    <div class="upload-info">
      <span class="upload-filename">{{ uploadStatus.fileName }}</span>
      <span class="upload-size">({{ formatSize(uploadStatus.fileSize) }})</span>
    </div>
    <div class="progress-bar">
      <div 
        class="progress-fill" 
        :style="{ width: uploadStatus.progress + '%' }"
      ></div>
    </div>
    <div class="progress-text">{{ uploadStatus.progress }}%</div>
  </div>
  <!-- 预览弹窗 -->
  <div v-if="showPreview" class="preview-backdrop" @click.self="closePreview">
    <div class="preview-modal">
      <button class="close-btn" @click="closePreview">✕</button>
      <!-- 根据文件类型渲染不同内容 -->
      <div v-if="previewType.startsWith('image/')" class="preview-content">
        <img :src="previewUrl" alt="图片预览" />
      </div>
      <div v-else-if="previewType.startsWith('text/')" class="preview-content">
        <pre class="text-preview">{{ textContent }}</pre>
      </div>
      <div v-else class="preview-content">
        <!-- 其他类型，比如 PDF，可以用 <iframe> 或者直接提示“无法预览” -->
        <p>文件类型 “{{ previewType }}” 暂不支持在线预览。</p>
        <a :href="downloadUrl" target="_blank">下载文件查看</a>
      </div>
    </div>
  </div>
</div>


  </div>
  <ShareDialog
    :fileId="currentFileId"
    :visible="shareDialogVisible"
    @cancel="shareDialogVisible=false"
    @confirm="doShare"
  />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import ShareDialog from './ShareDialog.vue'
import { useRouter } from 'vue-router';
import UploadFile from './UploadFile.vue'
import Cookies from 'js-cookie';

const pageSize = 6
const currentPage = ref(1)
const files = ref([])
const search = ref('')
const sortField = ref('')
const sortOrder = ref('asc')
const selectedFile = ref(null)
const surrounding = 1   // 当前页前后各显示多少页
const shareDialogVisible = ref(false)
const currentFileId = ref(null)

// Upload status tracking
const uploadStatus = ref({
  isUploading: false,
  fileName: '',
  fileSize: 0,
  progress: 0
})

const totalItems = ref(0)
const router = useRouter()
// ------------------------------
// 假设 files 是你要渲染的文件列表
// 在真实场景中，你可能从接口拉取文件列表：
// onMounted(() => { axios.get('/api/files/meta').then(res => files.value = res.data) })
// 这里只手动写一个 demo 列表


// 控制弹窗显隐
const showPreview = ref(false)
// 从后端返回的 Blob 对象转成 URL（仅图片时用）
const previewUrl = ref('')
// 如果是 text 类型，就存文本内容
const textContent = ref('')
// 保存后端返回的 mime-type
const previewType = ref('')
// 如果是不支持内联展示的类型，我们生成一个可下载链接供用户下载
const downloadUrl = ref('')
/**
 * 点击“预览”按钮后跳转到 /preview/{fileId}
 */
function previewFile(fileId) {
  console.log(fileId)
  router.push({
  name: 'FilePreview',
  params: { fileId: fileId }   // e.g. { fileId: 21 }
});
}

// 计算要展示的“页码 + 省略号”列表
const displayPages = computed(() => {
  const total = totalPages.value
  const cur   = currentPage.value
  const pages = []

  // ① 前两页
  for (let i = 1; i <= Math.min(2, total); i++) {
    pages.push(i)
  }

  // ② 前省略号
  if (cur - surrounding > 3) {
    pages.push('...')
  }

  // ③ 当前页前后各 surrounding 页
  const start = Math.max(3, cur - surrounding)
  const end   = Math.min(total - 2, cur + surrounding)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  // ④ 后省略号
  if (cur + surrounding < total - 2) {
    pages.push('...')
  }

  // ⑤ 后两页
  for (let i = Math.max(total - 1, 3); i <= total; i++) {
    if (!pages.includes(i)) pages.push(i)
  }

  return pages
})

// 当前页对应数据
const pagedFiles = computed(() => {
  const all = filteredFiles.value
  const start = (currentPage.value - 1) * pageSize
  return all.slice(start, start + pageSize)
})


function share(fileId) {
  currentFileId.value = fileId
  shareDialogVisible.value = true
}

async function doShare({ targetUserId, permission }) {
  console.log(targetUserId)
  console.log(permission)
  shareDialogVisible.value = false
  try {
    const { data } = await axios.post(`${import.meta.env.VITE_API_BASE}/file/share`, null, {
      headers:{ Authorization:`Bearer ${Cookies.get('token')}` },
      params:{ 
        targetUserId: targetUserId,
        fileId: currentFileId.value, 
        permission 
      }
    })
    alert(data)
  } catch (err) {
    alert('共享失败：'+(err.response?.data||err.message))
  }
}

async function fetchFiles() {
  try {
    const { data } =  await axios.get(`${import.meta.env.VITE_API_BASE}/file/list`, {
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    })
    if (data.code === 200) files.value = data.files
    else console.log(data.message)
  } catch (e) {
    console.error(e)
    alert('获取文件列表失败')
  }
}

function onFileSelect(e) {
  selectedFile.value = e.target.files[0]
}

// async function upload() {
//   if (!selectedFile.value) {
//     return alert('请选择文件')
//   }
//   const file = selectedFile.value
//   console.log(file)
//   // 自动推断 fileType
//   let fileName = file.name
//   const ext = fileName
//   .substring(fileName.lastIndexOf('.') + 1)
//   .toLowerCase()

//   // 后缀到 FileType 的映射
//   let fileType = 'OTHER'
//   const imageExts    = ['jpg','jpeg','png','gif','bmp','webp']
//   const videoExts    = ['mp4','mov','avi','mkv','flv','webm']
//   const audioExts    = ['mp3','wav','ogg','flac']
//   const documentExts = ['pdf','doc','docx','xls','xlsx','ppt','pptx','txt','csv','md']

//   if (imageExts.includes(ext)) {
//     fileType = 'IMAGE'
//   } else if (videoExts.includes(ext)) {
//     fileType = 'VIDEO'
//   } else if (audioExts.includes(ext)) {
//     fileType = 'AUDIO'
//   } else if (documentExts.includes(ext)) {
//     fileType = 'DOCUMENT'
//   }

//   const formData = new FormData()
//   formData.append('file', file)
//   formData.append('file_name', file.name)
//   formData.append('file_size', file.size)
//   formData.append('file_type', fileType)

//   try {
//     const { data } = await axios.post(`${import.meta.env.VITE_API_BASE}/file/upload`, formData, {
//       headers: { Authorization: `Bearer ${Cookies.get('token')}` }
//     })
//     alert('上传成功：ID=' + data.id)
//   } catch (e) {
//     console.error(e)
//     alert('上传失败：' + (e.response?.data || e.message))
//   }
// }
onMounted(fetchFiles)

const totalPages = computed(() => 
  Math.ceil(filteredFiles.value.length / pageSize)
)

const filteredFiles = computed(() => {
  let result = files.value.slice()
  if (search.value) {
    result = result.filter(f => f.fileName.includes(search.value))
  }
  if (sortField.value) {
    result.sort((a, b) => {
      let valA = a[sortField.value]
      let valB = b[sortField.value]

      // 如果是时间字段，先转时间戳
      if (sortField.value === 'uploadedAt') {
        valA = new Date(valA).getTime()
        valB = new Date(valB).getTime()
      }

      if (valA === valB) return 0
      if (valA < valB) return sortOrder.value === 'asc' ? -1 : 1
      return sortOrder.value === 'asc' ? 1 : -1
    })
  }
  return result
})


async function download(fileId) {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE}/file/download`, {
      params: { fileId },
      responseType: 'blob',  // 关键：告诉 Axios 返回二进制流
      headers: { Authorization: `Bearer ${Cookies.get('token')}` }
    });

    // 提取文件名
    // 应该用小写：
    const disposition = response.headers['content_disposition'];
    // console.log(res)
    console.log(disposition);

    let filename = `file-${fileId}`;
    if (disposition) {
      const match = disposition.match(/filename="?(.+)"?/);
      if (match) filename = match[1];
    }

    // 创建下载
    const url = URL.createObjectURL(response.data);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    URL.revokeObjectURL(url);
    a.remove();

  } catch (err) {
    console.error('下载失败', err);
    alert('下载失败：' + (err.response?.data || err.message));
  }
}

async function remove(fileId) {
  if (!confirm(`确认要删除文件 #${fileId}？`)) return

  try {
    // 注意：DELETE 方法里，URL 参数放在 params 中
    const response = await axios.delete(`${import.meta.env.VITE_API_BASE}/file/delete`, {
      headers: {
        'Authorization': `Bearer ${Cookies.get('token')}`
      },
      params: {
        fileId  // 与 @RequestParam Long fileId 对应
      }
    })

    // 根据后端返回的状态判断
    if (response.status === 200) {
      alert('删除成功')
      // TODO: 如果有文件列表，记得刷新
      // await fetchFiles()
    } else {
      alert('删除失败：' + response.data)
    }
  } catch (err) {
    console.error('删除请求失败', err)
    const msg = err.response?.data || err.message
    alert('删除失败：' + msg)
  }
}


function sortBy(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'asc'
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let num = bytes
  while (num >= 1024 && i < units.length - 1) { num /= 1024; i++ }
  // console.log(bytes)
  // console.log(`${num.toFixed(2)} ${units[i]}`)
  return `${num.toFixed(2)} ${units[i]}`
}

function formatDate(ts) {
  return ts ? new Date(ts).toLocaleString() : ''
}

// Upload event handlers
function handleUploadStart({ fileName, fileSize }) {
  uploadStatus.value = {
    isUploading: true,
    fileName,
    fileSize,
    progress: 0
  }
}

function handleUploadProgress(progress) {
  uploadStatus.value.progress = progress
}

function handleUploadComplete({ fileId, fileName }) {
  uploadStatus.value.isUploading = false
  // Refresh the file list to show the new file
  fetchFiles()
}

function handleUploadError(error) {
  uploadStatus.value.isUploading = false
  console.error('Upload error:', error)
}
</script>

<style scoped>
.file-manager {
  padding: var(--spacing-2xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-medium);
  border: 1px solid var(--border-light);
  animation: slideUp 0.5s ease;
  margin: var(--spacing-xl);
}

.header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-small);
  border: 1px solid var(--border-light);
}

.search-input {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-small);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-color-alpha);
}

.file-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: var(--spacing-xl);
  background: var(--bg-primary);
  border-radius: var(--border-radius-small);
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

.file-table th,
.file-table td {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  border-bottom: 1px solid var(--border-light);
}

.file-table th {
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: var(--font-semibold);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: background-color 0.2s ease;
  position: relative;
}

.file-table th:hover {
  background: var(--bg-hover);
}

.file-table td {
  color: var(--text-secondary);
  font-size: var(--font-sm);
  vertical-align: middle;
}

.file-table tbody tr {
  transition: background-color 0.2s ease;
}

.file-table tbody tr:hover {
  background: rgba(102, 126, 234, 0.1);
  color: var(--text-primary);
}

.actions {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.actions button {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: none;
  border-radius: var(--border-radius-small);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 60px;
}

.actions button:nth-child(1) {
  background: var(--success-color);
  color: white;
  border: 1px solid var(--success-color);
}

.actions button:nth-child(1):hover {
  background: var(--success-hover);
  border-color: var(--success-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(40, 167, 69, 0.3);
}

.actions button:nth-child(2) {
  background: var(--info-color);
  color: white;
  border: 1px solid var(--info-color);
}

.actions button:nth-child(2):hover {
  background: var(--info-hover);
  border-color: var(--info-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(23, 162, 184, 0.3);
}

.actions button:nth-child(3) {
  background: var(--danger-color);
  color: white;
  border: 1px solid var(--danger-color);
}

.actions button:nth-child(3):hover {
  background: var(--danger-hover);
  border-color: var(--danger-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(220, 53, 69, 0.3);
}

.actions button:nth-child(4) {
  background: var(--primary-gradient);
  color: white;
  border: 1px solid var(--primary-color);
}

.actions button:nth-child(4):hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.no-data {
  text-align: center;
  color: var(--text-muted);
  font-style: italic;
  padding: var(--spacing-2xl);
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-small);
  border: 1px solid var(--border-light);
}

.footer > span {
  color: var(--text-secondary);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
}

.pagination {
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
}

.pagination button {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--border-light);
  background: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--border-radius-small);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  transition: all 0.2s ease;
  min-width: 36px;
  text-align: center;
}

.pagination button:hover:not(:disabled) {
  background: var(--bg-hover);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
}

.pagination button.active {
  background: var(--primary-gradient);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-light);
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Upload Progress Styles */
.upload-progress-container {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-small);
  box-shadow: var(--shadow-light);
}

.upload-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.upload-filename {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  font-size: var(--font-sm);
}

.upload-size {
  color: var(--text-muted);
  font-size: var(--font-xs);
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: var(--bg-hover);
  border-radius: var(--border-radius-small);
  overflow: hidden;
  margin-bottom: var(--spacing-xs);
}

.progress-fill {
  height: 100%;
  background: var(--primary-gradient);
  transition: width 0.3s ease;
  border-radius: var(--border-radius-small);
}

.progress-text {
  text-align: right;
  font-size: var(--font-xs);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* Preview Modal Styles */
.preview-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.preview-modal {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-heavy);
  max-width: 90vw;
  max-height: 90vh;
  position: relative;
  overflow: hidden;
}

.close-btn {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  background: var(--danger-color);
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-lg);
  z-index: 10;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: var(--danger-hover);
  transform: scale(1.1);
}

.preview-content {
  padding: var(--spacing-xl);
  text-align: center;
}

.preview-content img {
  max-width: 100%;
  max-height: 70vh;
  border-radius: var(--border-radius-small);
  box-shadow: var(--shadow-medium);
}

.text-preview {
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-small);
  padding: var(--spacing-lg);
  max-height: 60vh;
  overflow-y: auto;
  text-align: left;
  font-family: 'Courier New', monospace;
  font-size: var(--font-sm);
  color: var(--text-primary);
  white-space: pre-wrap;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
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
  .file-manager {
    margin: var(--spacing-md);
    padding: var(--spacing-lg);
  }
  
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .file-table {
    font-size: var(--font-xs);
  }
  
  .file-table th,
  .file-table td {
    padding: var(--spacing-sm);
  }
  
  .actions {
    flex-direction: column;
  }
  
  .actions button {
    width: 100%;
  }
  
  .footer {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .pagination {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
