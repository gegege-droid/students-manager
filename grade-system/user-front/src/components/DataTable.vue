<script setup>
const props = defineProps({
  /** [{ key, title, width, align, nowrap }] */
  columns: { type: Array, required: true },
  rows: { type: Array, default: () => [] },
  /** 行唯一键字段名，或 (row, index) => key 的函数 */
  rowKey: { type: [String, Function], default: '' }
})

function resolveKey(row, index) {
  if (typeof props.rowKey === 'function') return props.rowKey(row, index)
  if (props.rowKey && row && row[props.rowKey] !== undefined) return row[props.rowKey]
  return index
}
</script>

<template>
  <div class="table-wrap">
    <table class="table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :style="col.width ? { width: col.width } : null"
            :class="{ c: col.align === 'center' }"
          >
            {{ col.title }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in rows" :key="resolveKey(row, index)">
          <td
            v-for="col in columns"
            :key="col.key"
            :class="[col.align === 'center' ? 'c' : '', col.nowrap ? 'nowrap' : '']"
          >
            <slot name="cell" :row="row" :column="col" :value="row[col.key]" :index="index">
              {{ row[col.key] === null || row[col.key] === undefined || row[col.key] === '' ? '—' : row[col.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
