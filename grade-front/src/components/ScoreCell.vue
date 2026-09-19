<script setup>
import { computed } from 'vue'
import { scoreColor, scoreLevel } from '@/utils/score'

const props = defineProps({
  value: { type: [Number, String], default: null },
  /** 是否同时显示「优秀 / 良好 / 不及格」文字标签 */
  showLevel: { type: Boolean, default: false }
})

const empty = computed(() => props.value === null || props.value === undefined || props.value === '')
const style = computed(() => ({
  color: scoreColor(Number(props.value)),
  fontWeight: 700
}))
const level = computed(() => scoreLevel(Number(props.value)))
</script>

<template>
  <span v-if="empty" class="text-secondary">—</span>
  <span v-else class="score-cell">
    <span class="score-value" :style="style">{{ value }}</span>
    <span v-if="showLevel" class="tag score-level" :style="{ color: style.color, borderColor: style.color }">
      {{ level }}
    </span>
  </span>
</template>

<style scoped>
.score-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.score-level {
  background: transparent;
  border-style: solid;
}
</style>
