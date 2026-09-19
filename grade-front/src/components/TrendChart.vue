<script setup>
import { computed } from 'vue'

/**
 * 总分趋势折线图（纯 SVG，不引第三方图表库）
 * 实线 = 历次月考总分；虚线 + 菱形 = 高考预测分；横向虚线 = 参考分数线
 */
const props = defineProps({
  labels: { type: Array, default: () => [] },
  values: { type: Array, default: () => [] },
  predicted: { type: Number, default: null },
  /** 高考总分上限 */
  max: { type: Number, default: 750 },
  /** 参考分数线 [{ label, score, color }] */
  lines: { type: Array, default: () => [] }
})

const W = 760
const H = 260
const PAD = { l: 58, r: 30, t: 26, b: 42 }
const plotW = W - PAD.l - PAD.r
const plotH = H - PAD.t - PAD.b

const hasPrediction = computed(() => props.predicted !== null && props.predicted !== undefined)
const slotCount = computed(() => props.values.length + (hasPrediction.value ? 1 : 0))

/** 纵轴范围：包住所有真实值、预测值和参考线，再留 25% 余量，这样趋势看得清 */
const domain = computed(() => {
  const nums = props.values.filter((v) => v !== null && v !== undefined)
  if (hasPrediction.value) nums.push(props.predicted)
  props.lines.forEach((line) => nums.push(line.score))
  if (!nums.length) return { lo: 0, hi: props.max }
  const lo = Math.min(...nums)
  const hi = Math.max(...nums)
  const padding = Math.max((hi - lo) * 0.25, 20)
  return {
    lo: Math.max(0, Math.floor((lo - padding) / 10) * 10),
    hi: Math.min(props.max, Math.ceil((hi + padding) / 10) * 10)
  }
})

function xOf(index) {
  const n = slotCount.value
  if (n <= 1) return PAD.l + plotW / 2
  return PAD.l + (index / (n - 1)) * plotW
}

function yOf(value) {
  const { lo, hi } = domain.value
  if (hi === lo) return PAD.t + plotH / 2
  return PAD.t + (1 - (value - lo) / (hi - lo)) * plotH
}

const actualPoints = computed(() =>
  props.values
    .map((value, index) => ({
      index,
      value,
      x: xOf(index),
      y: value === null || value === undefined ? null : yOf(value)
    }))
    .filter((point) => point.y !== null)
)

const polyline = computed(() => actualPoints.value.map((p) => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' '))

const predictedPoint = computed(() => {
  if (!hasPrediction.value) return null
  return { x: xOf(props.values.length), y: yOf(props.predicted), value: props.predicted }
})

const lastActual = computed(() => actualPoints.value[actualPoints.value.length - 1] || null)

const ticks = computed(() => {
  const { lo, hi } = domain.value
  const count = 4
  return Array.from({ length: count + 1 }, (_, k) => {
    const value = lo + ((hi - lo) * k) / count
    return { value: Math.round(value), y: yOf(value) }
  })
})

const slotLabels = computed(() => {
  const list = props.labels.map((label) => label.replace('第', '').replace('次月考', ''))
  if (hasPrediction.value) list.push('高考预测')
  return list
})

/** 首尾数据点的数值标签改用左右对齐，避免和纵轴刻度、预测标签撞在一起 */
function isFirstPoint(point) {
  return point.index === 0
}

function isLastActualPoint(point) {
  return hasPrediction.value && point.index === props.values.length - 1
}

function labelX(point) {
  if (isFirstPoint(point)) return point.x + 7
  if (isLastActualPoint(point)) return point.x - 7
  return point.x
}

function labelAnchor(point) {
  if (isFirstPoint(point)) return 'start'
  if (isLastActualPoint(point)) return 'end'
  return 'middle'
}
</script>

<template>
  <div class="chart">
    <svg :viewBox="`0 0 ${W} ${H}`" role="img" aria-label="总分趋势与预测">
      <!-- 横向网格与纵轴刻度 -->
      <g>
        <line
          v-for="tick in ticks"
          :key="`grid-${tick.value}`"
          :x1="PAD.l"
          :x2="W - PAD.r"
          :y1="tick.y"
          :y2="tick.y"
          stroke="#eef1f4"
          stroke-width="1"
        />
        <text
          v-for="tick in ticks"
          :key="`tick-${tick.value}`"
          :x="PAD.l - 10"
          :y="tick.y + 4"
          text-anchor="end"
          font-size="11"
          fill="#9aa0a8"
        >
          {{ tick.value }}
        </text>
      </g>

      <!-- 参考分数线 -->
      <g v-for="line in lines" :key="line.label">
        <line
          :x1="PAD.l"
          :x2="W - PAD.r"
          :y1="yOf(line.score)"
          :y2="yOf(line.score)"
          :stroke="line.color"
          stroke-width="1"
          stroke-dasharray="6 5"
          opacity="0.7"
        />
        <text :x="W - PAD.r - 4" :y="yOf(line.score) - 5" text-anchor="end" font-size="11" :fill="line.color">
          {{ line.label }} {{ line.score }}
        </text>
      </g>

      <!-- 预测虚线 -->
      <line
        v-if="predictedPoint && lastActual"
        :x1="lastActual.x"
        :y1="lastActual.y"
        :x2="predictedPoint.x"
        :y2="predictedPoint.y"
        stroke="#ed8936"
        stroke-width="2"
        stroke-dasharray="5 4"
      />

      <!-- 实际总分折线 -->
      <polyline :points="polyline" fill="none" stroke="#2b6cb0" stroke-width="2.5" stroke-linejoin="round" />

      <!-- 实际数据点 -->
      <g v-for="point in actualPoints" :key="`p-${point.index}`">
        <circle :cx="point.x" :cy="point.y" r="4.5" fill="#fff" stroke="#2b6cb0" stroke-width="2" />
        <text
          :x="labelX(point)"
          :y="point.y - 12"
          :text-anchor="labelAnchor(point)"
          font-size="11"
          fill="#2b6cb0"
          font-weight="600"
        >
          {{ point.value }}
        </text>
      </g>

      <!-- 预测点 -->
      <g v-if="predictedPoint">
        <rect
          :x="predictedPoint.x - 5"
          :y="predictedPoint.y - 5"
          width="10"
          height="10"
          fill="#ed8936"
          transform="rotate(45)"
          :transform-origin="`${predictedPoint.x} ${predictedPoint.y}`"
        />
        <text
          :x="predictedPoint.x"
          :y="predictedPoint.y - 14"
          text-anchor="middle"
          font-size="12"
          fill="#c05621"
          font-weight="700"
        >
          {{ predictedPoint.value }}
        </text>
      </g>

      <!-- 横轴标签 -->
      <text
        v-for="(label, index) in slotLabels"
        :key="`x-${label}-${index}`"
        :x="xOf(index)"
        :y="H - 14"
        text-anchor="middle"
        font-size="11"
        :fill="index === slotLabels.length - 1 && hasPrediction ? '#c05621' : '#9aa0a8'"
      >
        {{ label }}
      </text>
    </svg>

    <div class="legend">
      <span class="item"><i class="dot" style="background: #2b6cb0"></i>历次月考总分</span>
      <span class="item"><i class="dot" style="background: #ed8936"></i>高考预测总分</span>
      <span v-for="line in lines" :key="`lg-${line.label}`" class="item">
        <i class="dash" :style="{ background: line.color }"></i>{{ line.label }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.chart {
  width: 100%;
}

svg {
  display: block;
  width: 100%;
  height: auto;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dash {
  width: 14px;
  height: 2px;
  border-radius: 1px;
  display: inline-block;
}
</style>
