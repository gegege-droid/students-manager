<script setup>
import { computed, ref } from 'vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import TrendChart from '@/components/TrendChart.vue'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { daysUntil, PREDICT_CONFIG, rateColor } from '@/utils/score'

/**
 * P3 高考成绩预测
 * 算法：加权移动平均 + 趋势修正（越近的月考权重越大，再按最近 3 次的趋势做阻尼外推）
 */

const user = useUserStore()
const data = useScoreStore()

const prediction = computed(() => data.predictionOfStudent(user.loginId))
const targetScore = ref(600)

const predictedTotal = computed(() => prediction.value.total?.predicted ?? null)
const gap = computed(() =>
  predictedTotal.value === null ? null : predictedTotal.value - Number(targetScore.value || 0)
)
const daysLeft = computed(() => daysUntil(data.gradeInfo.examDate))

const chartLabels = computed(() => data.sessionList.map((item) => item.name))
const chartValues = computed(() => prediction.value.totalSeries.map((item) => item.value))

const columns = [
  { key: 'name', title: '科目', width: '150px' },
  { key: 'fullScore', title: '满分', width: '80px', align: 'center' },
  { key: 'avgScore', title: '历次均分', width: '130px', align: 'center' },
  { key: 'latest', title: '最近一次', width: '120px', align: 'center' },
  { key: 'deltaPerExam', title: '每次变化', width: '130px', align: 'center' },
  { key: 'predicted', title: '预测分', width: '140px', align: 'center' },
  { key: 'range', title: '预测区间', width: '160px', align: 'center' },
  { key: 'confidence', title: '置信度', width: '140px', align: 'center' }
]

function avgScore(item) {
  const list = item.records.filter((record) => record.score !== null)
  if (!list.length) return null
  return Math.round(list.reduce((sum, record) => sum + record.score, 0) / list.length)
}

function deltaText(value) {
  if (!value) return '持平'
  return value > 0 ? `↑ +${value}` : `↓ ${value}`
}

function deltaColor(value) {
  if (!value) return '#9aa0a8'
  return value > 0 ? '#38a169' : '#e53e3e'
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">高考成绩预测</h2>
      <span class="breadcrumb">首页 / 高考成绩预测</span>
    </div>

    <!-- 预测总分 -->
    <div class="hero">
      <div class="hero-main">
        <span class="hero-label">高考预测总分</span>
        <div class="hero-score">
          {{ predictedTotal ?? '—' }}
          <span class="hero-total">/ {{ data.gradeInfo.totalFullScore }}</span>
        </div>
        <div class="hero-range">
          预测区间
          <b>{{ prediction.total?.low ?? '—' }} ~ {{ prediction.total?.high ?? '—' }}</b>
          <StatusTag class="hero-conf" :value="prediction.confidence" />
        </div>
      </div>
      <div class="hero-side">
        <div class="side-item">
          <span class="side-label">距高考</span>
          <span class="side-value">{{ daysLeft !== null && daysLeft > 0 ? `${daysLeft} 天` : '已结束' }}</span>
        </div>
        <div class="side-item">
          <span class="side-label">已参加月考</span>
          <span class="side-value">{{ prediction.sessionCount }} 次</span>
        </div>
        <div class="side-item">
          <span class="side-label">选科组合</span>
          <span class="side-value">{{ data.studentMap[user.loginId]?.combination }}</span>
        </div>
      </div>
    </div>

    <!-- 目标分与参考线 -->
    <div class="card mt-16">
      <div class="card-head">
        <h3 class="card-title" style="margin: 0">目标分对比</h3>
        <span class="hint">目标分可自行调整，用来判断当前趋势下的差距</span>
      </div>
      <div class="goal-row">
        <span class="label" style="margin: 0">我的目标分</span>
        <input v-model.number="targetScore" class="input input-sm" style="width: 110px" type="number" min="0" max="750" />
        <span class="hint">分</span>
        <span v-if="gap !== null" class="gap" :class="gap >= 0 ? 'gap-ok' : 'gap-bad'">
          {{ gap >= 0 ? `按当前趋势，预计高于目标 ${gap} 分` : `按当前趋势，预计低于目标 ${-gap} 分` }}
        </span>
      </div>

      <ul class="line-list">
        <li v-for="line in data.referenceLines" :key="line.key">
          <span class="line-dot" :style="{ background: line.color }"></span>
          <span class="line-name">{{ line.label }}</span>
          <b>{{ line.score }} 分</b>
          <StatusTag
            :value="predictedTotal !== null && predictedTotal >= line.score ? 'ACCEPTED' : 'REJECTED'"
            :label="predictedTotal !== null && predictedTotal >= line.score ? '已达标' : '未达标'"
          />
        </li>
        <li>
          <span class="line-dot" style="background: #805ad5"></span>
          <span class="line-name">我的目标</span>
          <b>{{ targetScore }} 分</b>
          <StatusTag
            :value="gap !== null && gap >= 0 ? 'ACCEPTED' : 'REJECTED'"
            :label="gap !== null && gap >= 0 ? '预计可达' : '需要提升'"
          />
        </li>
      </ul>
      <p class="hint" style="margin: 0">参考分数线为示例值，并非任何省份的真实数据。</p>
    </div>

    <!-- 趋势图 -->
    <div class="card mt-16">
      <div class="card-head">
        <h3 class="card-title" style="margin: 0">总分趋势与预测</h3>
        <span class="hint">实线为历次月考总分，橙色虚线为按当前趋势外推的高考预测</span>
      </div>
      <TrendChart
        :labels="chartLabels"
        :values="chartValues"
        :predicted="predictedTotal"
        :max="data.gradeInfo.totalFullScore"
        :lines="data.referenceLines"
      />
    </div>

    <!-- 分科预测 -->
    <div class="card card-flush mt-16">
      <div class="card-head">
        <h3 class="card-title" style="margin: 0">分科预测明细</h3>
        <span class="hint">预测分 = 满分 × 预测得分率</span>
      </div>
      <DataTable :columns="columns" :rows="prediction.subjects" row-key="subjectId">
        <template #cell="{ row, column }">
          <template v-if="column.key === 'name'">
            <b>{{ row.name }}</b>
          </template>
          <template v-else-if="column.key === 'fullScore'">
            <span class="text-secondary">{{ row.fullScore }}</span>
          </template>
          <template v-else-if="column.key === 'avgScore'">
            <span v-if="avgScore(row) === null" class="text-secondary">—</span>
            <span v-else>{{ avgScore(row) }}</span>
          </template>
          <template v-else-if="column.key === 'latest'">
            <span v-if="row.latest === null" class="text-secondary">—</span>
            <span v-else>{{ row.latest }}</span>
          </template>
          <template v-else-if="column.key === 'deltaPerExam'">
            <span :style="{ color: deltaColor(row.deltaPerExam), fontWeight: 600 }">
              {{ deltaText(row.deltaPerExam) }}
            </span>
            <span class="hint"> 分/次</span>
          </template>
          <template v-else-if="column.key === 'predicted'">
            <b class="predicted" :style="{ color: rateColor(row.predictedRate) }">
              {{ row.predicted ?? '—' }}
            </b>
          </template>
          <template v-else-if="column.key === 'range'">
            <span v-if="row.low === null" class="text-secondary">—</span>
            <span v-else class="text-secondary">{{ row.low }} ~ {{ row.high }}</span>
          </template>
          <template v-else-if="column.key === 'confidence'">
            <StatusTag :value="row.confidence" />
          </template>
          <template v-else>{{ row[column.key] ?? '—' }}</template>
        </template>
      </DataTable>

      <div class="sum-row">
        <span class="sum-label">预测总分</span>
        <b class="sum-value">{{ predictedTotal ?? '—' }}</b>
        <span class="text-secondary">/ {{ data.gradeInfo.totalFullScore }} 分</span>
        <span class="spacer"></span>
        <span class="hint">区间 {{ prediction.total?.low ?? '—' }} ~ {{ prediction.total?.high ?? '—' }}</span>
      </div>
    </div>

    <!-- 算法说明 -->
    <div class="card mt-16">
      <h3 class="card-title">预测是怎么算出来的</h3>
      <ol class="algo">
        <li>
          <b>换算得分率</b>：把每次月考分数除以该科满分。语文满分 150、化学满分 100，只有换算成得分率才能放在一起比较、汇总成总分。
        </li>
        <li>
          <b>加权移动平均</b>：第 i 次月考的权重取 i，也就是越近的考试越重要，得到基准得分率。
        </li>
        <li>
          <b>趋势修正</b>：对最近 {{ PREDICT_CONFIG.recentCount }} 次得分率做最小二乘拟合，取斜率，外推约
          {{ PREDICT_CONFIG.gap }} 次考试，再乘以阻尼系数 {{ PREDICT_CONFIG.damping }}，避免把一次波动放大成离谱预测。
        </li>
        <li>
          <b>预测分与区间</b>：预测得分率 × 高考满分 = 预测分；用历史得分率的标准差给出 ±区间，并按场次数量与波动幅度给出置信度。
        </li>
      </ol>
      <p class="hint" style="margin: 0">
        ⚠️ 预测结果基于 6 次月考的历史数据推算，<b>仅供参考</b>，不代表真实高考结果；实际成绩还受复习状态、临场发挥、试题难度等因素影响。
      </p>
    </div>
  </div>
</template>

<style scoped>
.hero {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  border-radius: var(--radius-card);
  background: linear-gradient(135deg, #ebf4ff 0%, #f7fafc 100%);
  border: 1px solid #d5e0ee;
}

.hero-label {
  display: block;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 2px;
}

.hero-score {
  font-size: 46px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
}

.hero-total {
  font-size: 18px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.hero-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4a5568;
  margin-top: 4px;
}

.hero-side {
  display: flex;
  gap: 32px;
}

.side-item {
  display: flex;
  flex-direction: column;
  text-align: right;
}

.side-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.side-value {
  font-size: 18px;
  font-weight: 700;
  color: #2f4d73;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 16px 12px;
}

.goal-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.gap {
  font-weight: 600;
}

.gap-ok {
  color: var(--color-success);
}

.gap-bad {
  color: var(--color-danger);
}

.line-list {
  list-style: none;
  margin: 0 0 10px;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.line-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  font-size: 13px;
  background: #fbfcfd;
}

.line-dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  display: inline-block;
}

.line-name {
  color: var(--color-text-secondary);
}

.predicted {
  font-size: 17px;
  font-variant-numeric: tabular-nums;
}

.sum-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 14px 16px;
  border-top: 1px solid var(--color-border);
  background: #fafbfc;
}

.sum-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.sum-value {
  font-size: 22px;
  color: var(--color-primary);
  font-variant-numeric: tabular-nums;
}

.algo {
  margin: 0 0 12px;
  padding-left: 20px;
  color: #4a5568;
  font-size: 13px;
  line-height: 1.9;
}
</style>
