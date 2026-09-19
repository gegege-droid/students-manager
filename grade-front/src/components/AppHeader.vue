<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const user = useUserStore()
const router = useRouter()
const toast = useToast()

function onLogout() {
  user.logout()
  toast.info('已退出登录')
  router.push({ name: 'Login' })
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <div class="brand">
        <span class="logo">▤</span>
        <span>成绩管理系统</span>
      </div>
      <div class="spacer"></div>
      <span class="role">{{ user.roleText }}</span>
      <span class="who">欢迎，{{ user.name }}</span>
      <button type="button" class="btn btn-sm" @click="onLogout">退出登录</button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  height: 100%;
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 15px;
}

.spacer {
  flex: 1;
}

.role {
  font-size: var(--font-size-hint);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border: 1px solid #d3e0f0;
  border-radius: 10px;
  padding: 0 9px;
  line-height: 20px;
}

.who {
  color: #4a5568;
}
</style>
