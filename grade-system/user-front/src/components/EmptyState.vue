<script setup>
defineProps({
  title: { type: String, default: '暂无数据' },
  desc: { type: String, default: '' }
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">∅</div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="desc" class="empty-desc">{{ desc }}</div>
    <div v-if="$slots.default" class="mt-12">
      <slot />
    </div>
  </div>
</template>
