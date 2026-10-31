<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: String, default: '' },
  /** 覆盖默认文案，例如 KEEP / CORRECT 在表格与弹窗里叫法不同 */
  label: { type: String, default: '' }
})

/** 枚举字典 —— 对应 docs/04-data-and-vue.md §1.8 */
const MAP = {
  NORMAL: { text: '正常', color: '#4a5568', bg: '#f2f4f7', border: '#e2e8f0' },
  APPEALING: { text: '查分中', color: '#a9702e', bg: '#fbf1e6', border: '#f0dfcd' },
  MODIFIED: { text: '已更正', color: '#2b6cb0', bg: '#ebf4ff', border: '#d3e0f0' },
  PENDING: { text: '待受理', color: '#a9702e', bg: '#fbf1e6', border: '#f0dfcd' },
  ACCEPTED: { text: '已受理', color: '#3f7d55', bg: '#eaf4ee', border: '#cfe6d8' },
  REJECTED: { text: '已驳回', color: '#a95050', bg: '#fdf2f2', border: '#eed6d6' },
  KEEP: { text: '维持原成绩', color: '#4a5568', bg: '#f2f4f7', border: '#e2e8f0' },
  CORRECT: { text: '更正成绩', color: '#3f7d55', bg: '#eaf4ee', border: '#cfe6d8' },
  MANUAL: { text: '手工修改', color: '#4a5568', bg: '#f2f4f7', border: '#e2e8f0' },
  APPEAL: { text: '查分更正', color: '#2b6cb0', bg: '#ebf4ff', border: '#d3e0f0' },
  REQUIRED: { text: '必修', color: '#a95050', bg: '#fdf2f2', border: '#eed6d6' },
  ELECTIVE: { text: '选修', color: '#4a5568', bg: '#f2f4f7', border: '#e2e8f0' }
}

const style = computed(() => {
  const item = MAP[props.value] || { text: props.value || '—', color: '#4a5568', bg: '#f2f4f7', border: '#e2e8f0' }
  return { color: item.color, background: item.bg, borderColor: item.border }
})

const text = computed(() => props.label || MAP[props.value]?.text || props.value || '—')
</script>

<template>
  <span class="tag" :style="style">{{ text }}</span>
</template>
