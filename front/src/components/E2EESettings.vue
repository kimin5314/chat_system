<template>
  <div class="e2ee-settings">
    <div class="settings-header">
      <div class="header-content">
        <div class="icon-container">
          <el-icon class="shield-icon" :class="{ 'active': e2eeStore.isEnabled }">
            <Lock />
          </el-icon>
        </div>
        <div class="header-text">
          <h3>端到端加密</h3>
          <p>保护您的聊天消息，只有您和接收者可以阅读</p>
        </div>
      </div>
      
      <el-switch
        v-model="e2eeStore.isEnabled"
        :loading="e2eeStore.isLoading"
        :disabled="e2eeStore.isLoading"
        size="large"
        @change="handleToggleE2EE"
      />
    </div>

    <div class="settings-content">
      <!-- E2EE Status Card -->
      <div class="status-card" :class="{ 'enabled': e2eeStore.isEnabled }">
        <div class="status-icon">
          <el-icon>
            <component :is="e2eeStore.isEnabled ? 'Check' : 'Close'" />
          </el-icon>
        </div>
        <div class="status-content">
          <h4>{{ e2eeStore.isEnabled ? '加密已启用' : '加密已禁用' }}</h4>
          <p>
            {{ e2eeStore.isEnabled 
              ? '您的消息将使用端到端加密保护' 
              : '您的消息将以明文形式发送' 
            }}
          </p>
        </div>
      </div>

      <!-- Key Information (only show when enabled) -->
      <div v-if="e2eeStore.isEnabled && e2eeStore.publicKey" class="key-info-card">
        <h4>
          <el-icon><Key /></el-icon>
          密钥信息
        </h4>
        <div class="key-display">
          <label>公钥指纹:</label>
          <div class="key-fingerprint">
            {{ getKeyFingerprint(e2eeStore.publicKey) }}
          </div>
        </div>
        <div class="key-actions">
          <el-button size="small" @click="copyPublicKey">
            <el-icon><DocumentCopy /></el-icon>
            复制公钥
          </el-button>
          <el-button size="small" type="warning" @click="regenerateKeys">
            <el-icon><Refresh /></el-icon>
            重新生成密钥
          </el-button>
        </div>
      </div>

      <!-- Statistics -->
      <div v-if="e2eeStore.isEnabled" class="stats-card">
        <h4>
          <el-icon><DataAnalysis /></el-icon>
          加密统计
        </h4>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-label">本地消息</span>
            <span class="stat-value">{{ stats.messageCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">联系人密钥</span>
            <span class="stat-value">{{ stats.contactKeysCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">存储大小</span>
            <span class="stat-value">{{ formatSize(stats.storageSize) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">保留期限</span>
            <span class="stat-value">{{ stats.retentionDays }}天</span>
          </div>
        </div>
      </div>

      <!-- Help Information -->
      <div class="help-card">
        <h4>
          <el-icon><QuestionFilled /></el-icon>
          关于端到端加密
        </h4>
        <div class="help-content">
          <div class="help-item">
            <el-icon class="help-icon"><Lock /></el-icon>
            <div>
              <strong>安全保护</strong>
              <p>消息在您的设备上加密，只有您和接收者可以解密</p>
            </div>
          </div>
          <div class="help-item">
            <el-icon class="help-icon"><Key /></el-icon>
            <div>
              <strong>密钥管理</strong>
              <p>密钥安全存储在您的设备上，服务器无法访问</p>
            </div>
          </div>
          <div class="help-item">
            <el-icon class="help-icon"><Warning /></el-icon>
            <div>
              <strong>重要提醒</strong>
              <p>如果丢失设备或清除浏览器数据，您将无法恢复历史加密消息</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Advanced Options -->
      <div v-if="e2eeStore.isEnabled" class="advanced-options">
        <h4>
          <el-icon><Setting /></el-icon>
          高级选项
        </h4>
        <div class="advanced-content">
          <div class="option-group">
            <h5>本地消息管理</h5>
            <p class="option-description">管理本地存储的加密消息数据</p>
            <div class="option-buttons">
              <el-button size="small" @click="exportMessages">
                <el-icon><Download /></el-icon>
                导出消息
              </el-button>
              <el-button size="small" @click="showImportDialog = true">
                <el-icon><Upload /></el-icon>
                导入消息
              </el-button>
              <el-button size="small" type="danger" @click="confirmClearMessages">
                <el-icon><Delete /></el-icon>
                清除本地消息
              </el-button>
            </div>
          </div>
          
          <div class="option-group reset-group">
            <h5>完全重置</h5>
            <p class="option-description">这将删除所有密钥和本地数据，操作无法恢复</p>
            <div class="warning-text">
              <el-icon><Warning /></el-icon>
              重置后将丢失所有加密数据，请确保已备份重要信息
            </div>
            <div class="option-buttons">
              <el-button size="small" type="danger" @click="confirmResetE2EE">
                <el-icon><Warning /></el-icon>
                完全重置E2EE
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Import Dialog -->
    <el-dialog
      v-model="showImportDialog"
      title="导入消息"
      width="500px"
    >
      <div class="import-content">
        <p>选择之前导出的消息文件进行导入：</p>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          accept=".json"
          @change="handleFileChange"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>
        <div v-if="importFile" class="file-info">
          <p>已选择文件: {{ importFile.name }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showImportDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="importMessages"
            :disabled="!importFile"
          >
            导入
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useE2EEStore } from '@/store/e2ee'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock,
  Check,
  Close,
  Key,
  DocumentCopy,
  Refresh,
  DataAnalysis,
  QuestionFilled,
  Warning,
  Download,
  Upload,
  Delete,
  Setting
} from '@element-plus/icons-vue'
import localMessageStorage from '@/utils/localMessageStorage'

const e2eeStore = useE2EEStore()

// Reactive data
const showImportDialog = ref(false)
const importFile = ref(null)

// Computed
const stats = computed(() => e2eeStore.getStats())

// Methods
async function handleToggleE2EE(enabled) {
  try {
    if (enabled) {
      const confirm = await ElMessageBox.confirm(
        '启用端到端加密后，您的消息将被加密保护。密钥将存储在您的设备上。继续吗？',
        '启用端到端加密',
        {
          confirmButtonText: '启用',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      
      if (confirm) {
        await e2eeStore.enableE2EE()
      } else {
        // Revert switch state
        e2eeStore.isEnabled = false
      }
    } else {
      const confirm = await ElMessageBox.confirm(
        '禁用端到端加密后，您的消息将以明文形式发送。已加密的历史消息仍需要密钥才能查看。确定要禁用吗？',
        '禁用端到端加密',
        {
          confirmButtonText: '禁用',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      if (confirm) {
        await e2eeStore.disableE2EE()
      } else {
        // Revert switch state
        e2eeStore.isEnabled = true
      }
    }
  } catch (error) {
    console.error('Failed to toggle E2EE:', error)
    // Revert switch state on error
    e2eeStore.isEnabled = !enabled
  }
}

function getKeyFingerprint(publicKey) {
  if (!publicKey) return ''
  // Create a simple fingerprint from the public key
  const hash = btoa(publicKey).substring(0, 32)
  return hash.match(/.{4}/g)?.join(':') || hash
}

async function copyPublicKey() {
  try {
    if (e2eeStore.publicKey) {
      await navigator.clipboard.writeText(e2eeStore.publicKey)
      ElMessage.success('公钥已复制到剪贴板')
    }
  } catch (error) {
    console.error('Failed to copy public key:', error)
    ElMessage.error('复制失败')
  }
}

async function regenerateKeys() {
  try {
    const confirm = await ElMessageBox.confirm(
      '重新生成密钥后，您将无法解密使用旧密钥加密的消息。确定要继续吗？',
      '重新生成密钥',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirm) {
      await e2eeStore.disableE2EE()
      await e2eeStore.enableE2EE()
      ElMessage.success('密钥已重新生成')
    }
  } catch (error) {
    console.error('Failed to regenerate keys:', error)
    ElMessage.error('密钥重新生成失败')
  }
}

function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function exportMessages() {
  try {
    const messagesJson = localMessageStorage.exportMessages()
    const blob = new Blob([messagesJson], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    
    const a = document.createElement('a')
    a.href = url
    a.download = `e2ee_messages_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    ElMessage.success('消息已导出')
  } catch (error) {
    console.error('Failed to export messages:', error)
    ElMessage.error('导出失败')
  }
}

function handleFileChange(file) {
  importFile.value = file.raw
}

async function importMessages() {
  try {
    if (!importFile.value) return
    
    const text = await importFile.value.text()
    localMessageStorage.importMessages(text, true) // Merge with existing
    
    showImportDialog.value = false
    importFile.value = null
    ElMessage.success('消息导入成功')
  } catch (error) {
    console.error('Failed to import messages:', error)
    ElMessage.error('导入失败: ' + error.message)
  }
}

async function confirmClearMessages() {
  try {
    const confirm = await ElMessageBox.confirm(
      '这将删除所有本地存储的原始消息。您确定要继续吗？',
      '清除本地消息',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirm) {
      localMessageStorage.clearAllMessages()
      ElMessage.success('本地消息已清除')
    }
  } catch (error) {
    // User cancelled
  }
}

async function confirmResetE2EE() {
  try {
    const confirm = await ElMessageBox.confirm(
      '这将完全重置E2EE功能，删除所有密钥和本地数据。此操作无法撤销！',
      '完全重置E2EE',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    if (confirm) {
      e2eeStore.clearAllData()
      ElMessage.success('E2EE已完全重置')
    }
  } catch (error) {
    // User cancelled
  }
}

// Initialize on mount
onMounted(async () => {
  await e2eeStore.initialize()
})
</script>

<style scoped>
.e2ee-settings {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-2xl);
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: var(--bg-secondary);
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius-small);
  background: rgba(102, 126, 234, 0.1);
  transition: all var(--transition-medium);
}

.shield-icon {
  font-size: 28px;
  color: var(--text-secondary);
  transition: color var(--transition-medium);
}

.shield-icon.active {
  color: var(--success-color);
}

.header-text h3 {
  margin: 0 0 var(--spacing-xs) 0;
  color: var(--text-primary);
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.header-text p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2xl);
}

.status-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-2xl);
  background: var(--bg-primary);
  border: 2px solid var(--border-color);
  border-radius: var(--border-radius);
  transition: all var(--transition-medium);
  box-shadow: var(--shadow-light);
}

.status-card.enabled {
  border-color: var(--success-color);
  background: rgba(40, 167, 69, 0.05);
}

.status-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: var(--border-radius-small);
  background: var(--bg-secondary);
  font-size: 24px;
}

.status-card.enabled .status-icon {
  background: var(--success-color);
  color: white;
}

.status-content h4 {
  margin: 0 0 var(--spacing-xs) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.status-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.key-info-card,
.stats-card,
.help-card {
  padding: var(--spacing-2xl);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
}

.key-info-card h4,
.stats-card h4,
.help-card h4 {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.key-display {
  margin-bottom: var(--spacing-lg);
}

.key-display label {
  display: block;
  margin-bottom: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.key-fingerprint {
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-small);
  font-family: monospace;
  font-size: var(--font-size-sm);
  word-break: break-all;
  color: var(--text-primary);
}

.key-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-lg);
}

.stat-item {
  text-align: center;
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-small);
}

.stat-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.stat-value {
  display: block;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--primary-color);
}

.help-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.help-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.help-icon {
  font-size: 20px;
  color: var(--primary-color);
  flex-shrink: 0;
  margin-top: var(--spacing-xs);
}

.help-item strong {
  display: block;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.help-item p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

.advanced-options {
  padding: var(--spacing-2xl);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-light);
}

.advanced-options h4 {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.advanced-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2xl);
}

.option-group {
  padding: var(--spacing-2xl);
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-color);
  transition: all var(--transition-medium);
}

.option-group:hover {
  box-shadow: var(--shadow-medium);
  border-color: var(--primary-color);
}

.option-group h5 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.option-group h5::before {
  content: '';
  width: 4px;
  height: 20px;
  background: var(--primary-color);
  border-radius: var(--border-radius-small);
}

.option-buttons {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  margin-top: var(--spacing-md);
}

.warning-text {
  margin: var(--spacing-sm) 0 var(--spacing-md) 0;
  color: var(--warning-color);
  font-size: var(--font-size-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  background: rgba(255, 193, 7, 0.1);
  border-radius: var(--border-radius-small);
  border-left: 3px solid var(--warning-color);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.option-description {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

.reset-group {
  border-color: rgba(220, 53, 69, 0.2);
}

.reset-group:hover {
  border-color: var(--danger-color);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.15);
}

.import-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.file-info {
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-small);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .e2ee-settings {
    padding: var(--spacing-lg);
  }
  
  .settings-header {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-lg);
    padding: var(--spacing-xl);
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .option-buttons {
    flex-direction: column;
  }
  
  .key-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .help-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>
