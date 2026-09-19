<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BlankLayout from '@/layouts/BlankLayout.vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import ToastHost from '@/components/ToastHost.vue'

const route = useRoute()
// 登录页 / 404 用空白布局，其余页面套「顶部导航 + 侧边菜单」框架（对应 Axure 母版）
const layout = computed(() => (route.meta.public ? BlankLayout : DefaultLayout))
</script>

<template>
  <component :is="layout">
    <router-view />
  </component>
  <ToastHost />
</template>
