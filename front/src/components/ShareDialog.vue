<template>
  <div v-if="visible" class="modal-backdrop">
    <div class="modal">
      <h3 class="modal-title">共享文件 {{ fileId }}</h3>
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
  fileId: { type: Number, required: true },
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
  emits('confirm', {
    targetUserId: targetUserId.value,
    permission: permission.value
  })
}
</script>

<style scoped>
/* 背景遮罩 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 弹窗主体 */
.modal {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  width: 360px;
  max-width: 90%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

/* 标题 */
.modal-title {
  margin: 0;
  margin-bottom: 16px;
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  text-align: center;
}

/* 内容区域 */
.modal-body {
  flex: 1;
}

/* 每一行表单项 */
.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

/* 表单标签文字 */
.form-label {
  font-size: 14px;
  color: #555555;
  margin-bottom: 6px;
}

/* 输入框通用样式 */
.form-input,
.form-select {
  width: 90%;
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s ease;
}

/* 输入框获得焦点时的样式 */
.form-input:focus,
.form-select:focus {
  border-color: #3b82f6;
}

/* 下拉框内文字对齐 */
.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D'10'%20height%3D'6'%20viewBox%3D'0%200%2010%206'%20xmlns%3D'http%3A//www.w3.org/2000/svg'%3E%3Cpath%20d%3D'M0%200l5%206%205%20-6'%20fill%3D'%23555555'%20/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 10px 6px;
  cursor: pointer;
}

/* 底部按钮区域 */
.modal-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

/* 公共按钮样式 */
.btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

/* 次级按钮（取消） */
.btn-secondary {
  background-color: #f3f4f6;
  color: #4b5563;
}

/* 次级按钮悬停 */
.btn-secondary:hover {
  background-color: #e5e7eb;
}

/* 主要按钮（确定） */
.btn-primary {
  background-color: #3b82f6;
  color: #ffffff;
  margin-left: 12px;
}

/* 主要按钮悬停 */
.btn-primary:hover {
  background-color: #2563eb;
}
</style>
