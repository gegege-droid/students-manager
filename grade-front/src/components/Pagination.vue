<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

const emit = defineEmits(['update:page'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

function go(target) {
  if (target < 1 || target > totalPages.value) return
  emit('update:page', target)
}
</script>

<template>
  <div class="pagination">
    <span>共 {{ total }} 条</span>
    <button type="button" class="btn btn-sm" :disabled="page <= 1" @click="go(page - 1)">‹ 上一页</button>
    <span>{{ page }} / {{ totalPages }}</span>
    <button type="button" class="btn btn-sm" :disabled="page >= totalPages" @click="go(page + 1)">下一页 ›</button>
  </div>
</template>
