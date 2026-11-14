<script setup>
import { onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: Number, default: 560 },
  /** 底部按钮区是否显示 */
  showFooter: { type: Boolean, default: true },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' },
  confirmLoading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

function close() {
  emit('update:modelValue', false)
  emit('cancel')
}

function onConfirm() {
  emit('confirm')
}

/** ESC 关闭，提升键盘可用性 */
function onKeydown(event) {
  if (event.key === 'Escape' && props.modelValue) close()
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
    }
  }
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <teleport to="body">
    <transition name="modal">
      <div v-if="modelValue" class="modal-mask" @click.self="close">
        <div class="modal" :style="{ width: `${width}px` }">
          <div class="modal-head">
            <span class="modal-title">{{ title }}</span>
            <button type="button" class="modal-close" @click="close">✕</button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="showFooter" class="modal-foot">
            <button type="button" class="btn" @click="close">{{ cancelText }}</button>
            <button type="button" class="btn btn-primary" :disabled="confirmLoading" @click="onConfirm">
              {{ confirmLoading ? '处理中…' : confirmText }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  z-index: 1000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 80px 16px 40px;
  overflow-y: auto;
}

.modal {
  max-width: 100%;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-modal);
  display: flex;
  flex-direction: column;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  font-size: var(--font-size-section);
  font-weight: 700;
}

.modal-close {
  border: none;
  background: none;
  font-size: 15px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
  padding: 4px;
}

.modal-close:hover {
  color: var(--color-text);
}

.modal-body {
  padding: 20px;
}

.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid var(--color-border);
  background: #fafbfc;
  border-radius: 0 0 var(--radius-card) var(--radius-card);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s;
}

.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform 0.2s;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal,
.modal-leave-to .modal {
  transform: translateY(-12px);
}
</style>
