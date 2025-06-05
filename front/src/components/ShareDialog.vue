<template>
  <div v-if="visible" class="modal-backdrop">
    <div class="modal">
      <h3 class="modal-title">共享文件 {{ fileId || '未选择' }}</h3>
      <div class="modal-body">
        <label class="form-group">
          <span class="form-label">目标用户 ID：</span>
          <input
            v-model="targetUserId"
            type="number"
            class="form-input"
            placeholder="请输入用户 ID"
          />
        </label>
        <label class="form-group">
          <span class="form-label">权限类型：</span>
          <select v-model="permission" class="form-select">
            <option value="READ">只读</option>
            <option value="DELETE">删除</option>
            <option value="SHARE">分享</option>
          </select>
        </label>
      </div>
      <div class="modal-footer">
        <button @click="onCancel" class="btn btn-secondary">取消</button>
        <button @click="onConfirm" class="btn btn-primary">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineEmits, defineProps, ref, watch } from 'vue'

const props = defineProps({
  fileId: { type: Number, default: null },
  visible: { type: Boolean, required: true }
})
const emits = defineEmits(['cancel', 'confirm'])

const targetUserId = ref('')
const permission = ref('READ')

// 每次打开时清空上次输入
watch(() => props.visible, (val) => {
  if (val) {
    targetUserId.value = ''
    permission.value = 'READ'
  }
})

function onCancel() {
  emits('cancel')
}
function onConfirm() {
  if (!props.fileId) {
    alert('请先选择要分享的文件')
    return
  }
  emits('confirm', {
    targetUserId: targetUserId.value,
    permission: permission.value
  })
}
</script>

<style scoped>
/* Background overlay */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

/* Modal container */
.modal {
  background: var(--bg-primary);
  padding: var(--spacing-2xl);
  border-radius: var(--border-radius);
  width: 400px;
  max-width: 90%;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  animation: scaleIn 0.3s ease;
}

/* Title */
.modal-title {
  margin: 0;
  margin-bottom: var(--spacing-xl);
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  text-align: center;
}

/* Content area */
.modal-body {
  flex: 1;
}

/* Form group styling */
.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--spacing-lg);
}

/* Form labels */
.form-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
  font-weight: var(--font-medium);
}

/* Input and select styling */
.form-input,
.form-select {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-sm);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-small);
  background: var(--bg-secondary);
  color: var(--text-primary);
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

/* Focus states */
.form-input:focus,
.form-select:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-color-alpha);
  background: var(--bg-primary);
}

/* Select dropdown styling */
.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D'10'%20height%3D'6'%20viewBox%3D'0%200%2010%206'%20xmlns%3D'http%3A//www.w3.org/2000/svg'%3E%3Cpath%20d%3D'M0%200l5%206%205%20-6'%20fill%3D'%23667eea'%20/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right var(--spacing-md) center;
  background-size: 10px 6px;
  cursor: pointer;
  padding-right: var(--spacing-2xl);
}

/* Footer buttons area */
.modal-footer {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

/* Common button styling */
.btn {
  padding: var(--spacing-sm) var(--spacing-lg);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  border-radius: var(--border-radius-small);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Secondary button (Cancel) */
.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
}

.btn-secondary:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
}

/* Primary button (Confirm) */
.btn-primary {
  background: var(--primary-gradient);
  color: white;
  box-shadow: var(--shadow-light);
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

/* Animation keyframes */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Responsive design */
@media (max-width: 480px) {
  .modal {
    width: 95%;
    padding: var(--spacing-xl);
  }
  
  .modal-title {
    font-size: var(--font-lg);
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>
