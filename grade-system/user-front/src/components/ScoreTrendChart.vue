<script setup>
import { computed } from 'vue'

/** 最近成绩折线图 —— 纯 SVG，无额外图表依赖 */
const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: Number, default: 220 }
})

const W = 640
const PAD = { left: 44, right: 22, top: 26, bottom: 36 }

const list = computed(() =>
  props.data.filter((item) => item && item.value !== null && item.value !== undefined)
)

const scaleY = (value) => {
  const h = props.height - PAD.top - PAD.bottom
  return PAD.top + (1 - Math.min(100, Math.max(0, Number(value))) / 100) * h
}

const points = computed(() => {
  const items = list.value
  if (!items.length) return []
  const innerW = W - PAD.left - PAD.right
  return items.map((item, index) => ({
    ...item,
    x: PAD.left + (items.length === 1 ? innerW / 2 : (index * innerW) / (items.length - 1)),
    y: scaleY(item.value)
  }))
})

const polyline = computed(() => points.value.map((p) => `${p.x},${p.y}`).join(' '))
const areaPath = computed(() => {
  if (!points.value.length) return ''
  const base = props.height - PAD.bottom
  return `M ${points.value[0].x} ${base} L ${points.value
    .map((p) => `${p.x} ${p.y}`)
    .join(' L ')} L ${points.value[points.value.length - 1].x} ${base} Z`
})

const average = computed(() => {
  if (!list.value.length) return null
  const sum = list.value.reduce((acc, item) => acc + Number(item.value), 0)
  return Number((sum / list.value.length).toFixed(1))
})

const averageY = computed(() => (average.value === null ? null : scaleY(average.value)))
const gridLines = computed(() => [0, 20, 40, 60, 80, 100].map((v) => ({ v, y: scaleY(v) })))

function shortLabel(label) {
  const text = String(label || '')
  return text.length > 5 ? `${text.slice(0, 5)}…` : text
}
</script>

<template>
  <div class="trend-chart">
    <svg
      v-if="points.length"
      :viewBox="`0 0 ${W} ${height}`"
      role="img"
      aria-label="最近成绩折线图"
    >
      <defs>
        <linearGradient id="trendArea" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#2b6cb0" stop-opacity="0.22" />
          <stop offset="100%" stop-color="#2b6cb0" stop-opacity="0.02" />
        </linearGradient>
      </defs>

      <!-- 横向网格与纵轴刻度 -->
      <g v-for="line in gridLines" :key="line.v">
        <line
          :x1="PAD.left"
          :y1="line.y"
          :x2="W - PAD.right"
          :y2="line.y"
          stroke="#e2e8f0"
          stroke-dasharray="4 4"
        />
        <text :x="PAD.left - 8" :y="line.y + 4" text-anchor="end" class="axis-text">
          {{ line.v }}
        </text>
      </g>

      <!-- 平均分参考线 -->
      <g v-if="averageY !== null">
        <line
          :x1="PAD.left"
          :y1="averageY"
          :x2="W - PAD.right"
          :y2="averageY"
          stroke="#38a169"
          stroke-dasharray="6 5"
        />
        <text :x="W - PAD.right" :y="averageY - 6" text-anchor="end" class="avg-text">
          平均 {{ average }}
        </text>
      </g>

      <path :d="areaPath" fill="url(#trendArea)" />
      <polyline
        :points="polyline"
        fill="none"
        stroke="#2b6cb0"
        stroke-width="2.5"
        stroke-linejoin="round"
        stroke-linecap="round"
      />

      <!-- 数据点与数值 -->
      <g v-for="p in points" :key="p.label + p.x">
        <circle :cx="p.x" :cy="p.y" r="4.5" fill="#fff" stroke="#2b6cb0" stroke-width="2.5" />
        <text :x="p.x" :y="p.y - 11" text-anchor="middle" class="point-text">
          {{ p.value }}
        </text>
        <text :x="p.x" :y="height - 12" text-anchor="middle" class="axis-text">
          {{ shortLabel(p.label) }}
        </text>
      </g>
    </svg>

    <div v-else class="trend-empty">暂无足够成绩数据，无法绘制趋势</div>
  </div>
</template>

<style scoped>
.trend-chart {
  width: 100%;
}

svg {
  width: 100%;
  height: auto;
  display: block;
}

.axis-text {
  font-size: 11px;
  fill: #718096;
}

.point-text {
  font-size: 11px;
  fill: #2b6cb0;
  font-weight: 600;
}

.avg-text {
  font-size: 11px;
  fill: #38a169;
  font-weight: 600;
}

.trend-empty {
  padding: 48px 0;
  text-align: center;
  color: #718096;
  font-size: 13px;
}
</style>
