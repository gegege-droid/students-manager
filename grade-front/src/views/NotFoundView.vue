<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()

function goHome() {
  router.push(user.isLoggedIn ? { name: user.homeRoute } : { name: 'Login' })
}
</script>

<template>
  <div class="notfound">
    <div class="code">404</div>
    <p class="title">页面不存在</p>
    <p class="hint">你访问的地址没有对应的页面，可能是链接输入有误。</p>
    <button type="button" class="btn btn-primary mt-16" @click="goHome">返回首页</button>
  </div>
</template>

<style scoped>
.notfound {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 16px;
}

.code {
  font-size: 64px;
  font-weight: 700;
  color: var(--color-primary-light);
  line-height: 1;
}

.title {
  font-size: var(--font-size-title);
  font-weight: 700;
  margin-top: 8px;
}
</style>
