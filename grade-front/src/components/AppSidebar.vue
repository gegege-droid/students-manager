<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const user = useUserStore()

/** 菜单按角色渲染 */
const MENUS = {
  STUDENT: [
    { name: 'StudentExamQuery', label: '月考成绩查询' },
    { name: 'StudentPrediction', label: '高考成绩预测' },
    { name: 'StudentAppealApply', label: '申请查分' }
  ],
  TEACHER: [
    { name: 'TeacherAppealHandle', label: '受理查分申请' },
    { name: 'TeacherScoreAdd', label: '录入月考成绩' },
    { name: 'TeacherScoreEdit', label: '修改成绩' }
  ]
}

const menus = computed(() => MENUS[user.role] || [])
</script>

<template>
  <aside class="app-sidebar">
    <nav>
      <router-link v-for="item in menus" :key="item.name" class="nav-item" :to="{ name: item.name }">
        {{ item.label }}
      </router-link>
      <span class="nav-item is-disabled" title="本原型未实现，列为后续扩展">个人中心</span>
    </nav>
  </aside>
</template>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  flex: 0 0 var(--sidebar-width);
  background: #fff;
  border-right: 1px solid var(--color-border);
  padding: 16px 0;
  min-height: calc(100vh - var(--header-height));
}

.nav-item {
  display: block;
  padding: 10px 24px;
  color: #4a5568;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.nav-item:hover {
  background: #f7fafc;
  color: var(--color-primary);
}

.nav-item.router-link-active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 600;
  border-left-color: var(--color-primary);
}

.nav-item.is-disabled {
  color: #b7bec7;
  cursor: not-allowed;
}

.nav-item.is-disabled:hover {
  background: transparent;
  color: #b7bec7;
}
</style>
