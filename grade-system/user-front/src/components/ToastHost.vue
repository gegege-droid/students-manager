<script setup>
import { useToast } from '@/composables/useToast'

const { toasts } = useToast()
</script>

<template>
  <teleport to="body">
    <div class="toast-host">
      <transition-group name="toast">
        <div v-for="item in toasts" :key="item.id" class="toast" :class="`toast-${item.type}`">
          <span>{{ item.type === 'success' ? '✓' : item.type === 'error' ? '✕' : 'ℹ' }}</span>
          <span>{{ item.message }}</span>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
