<script setup>
import { computed, reactive, ref, watch } from 'vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterBar from '@/components/FilterBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { gradeLevel, rateColor } from '@/utils/score'

/** P2 月考成绩查询 —— 科目 × 月考场次 的成绩单矩阵 */

const user = useUserStore()
const data = useScoreStore()

const filters = reactive({ examId: '', subjectId: '' })
const applied = reactive({ examId: '', subjectId: '' })

const student = computed(() => data.studentMap[user.loginId] || {})
const mySubjects = computed(() =>
  (student.value.subjects || []).map((id) => data.subjectMap[id]).filter(Boolean)
)

const matrix = computed(() => data.matrixOfStudent(user.loginId, applied))

/** 最近一次有成绩的月考汇总 */
const latestSummary = computed(() => {
  const totals = matrix.value.totals.filter((item) => item.total !== null)
  return totals[totals.length - 1] || null
})

const latestLevel = computed(() =>
  latestSummary.value ? gradeLevel(latestSummary.value.total / data.gradeInfo.totalFullScore) : null
)

const matrixColumns = computed(() => {
  const cols = [
    { key: 'name', title: '科目', width: '150px' },
    { key: 'fullScore', title: '满分', width: '80px', align: 'center' }
  ]
  matrix.value.sessions.forEach((session) => {
    cols.push({ key: `exam-${session.examId}`, title: session.name, width: '130px', align: 'center' })
  })
  cols.push({ key: 'delta', title: '较上次', width: '142px', align: 'center' })
  return cols
})

const rankColumns = [
  { key: 'name', title: '月考场次', width: '200px' },
  { key: 'date', title: '考试日期', width: '140px' },
  { key: 'total', title: '总分', width: '110px', align: 'center' },
  { key: 'rate', title: '得分率', width: '110px', align: 'center' },
  { key: 'classRank', title: '班级排名', width: '140px', align: 'center' },
  { key: 'gradeRank', title: '年级排名', width: '160px', align: 'center' },
  { key: 'level', title: '等级', width: '120px', align: 'center' }
]

const rankRows = computed(() =>
  matrix.value.totals.map((item) => {
    const session = data.sessionMap[item.examId] || {}
    return {
      ...item,
      date: session.date,
      level: item.total === null ? null : gradeLevel(item.rate).label
    }
  })
)

function cellOf(row, examId) {
  return row.cells.find((cell) => cell.examId === examId) || { score: null, rate: null, status: 'NORMAL' }
}

function onSearch() {
  applied.examId = filters.examId
  applied.subjectId = filters.subjectId
}

function onReset() {
  filters.examId = ''
  filters.subjectId = ''
  onSearch()
}

watch(() => filters.subjectId, onSearch)

function deltaText(delta) {
  if (!delta) return '—'
  return delta > 0 ? `↑ +${delta}` : `↓ ${delta}`
}

function deltaColor(delta) {
  if (!delta) return '#9aa0a8'
  return delta > 0 ? '#38a169' : '#e53e3e'
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">月考成绩查询</h2>
      <span class="breadcrumb">首页 / 月考成绩查询</span>
    </div>

    <!-- 学生信息 -->
    <div class="student-bar">
      <span class="who">{{ student.name }}</span>
      <span class="meta">学号 {{ student.studentId }}</span>
      <span class="meta">{{ student.className }}</span>
      <span class="chip">选科 {{ student.combination }}</span>
      <span class="meta">班主任 {{ data.userNameOf('T001') }}</span>
    </div>

    <!-- 筛选 -->
    <FilterBar>
      <div class="filter-item">
        <label class="label" for="exam">月考场次</label>
        <select id="exam" v-model="filters.examId" class="select" style="width: 180px">
          <option value="">全部场次</option>
          <option v-for="item in data.scoredSessionList" :key="item.examId" :value="item.examId">
            {{ item.name }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="subject">科目</label>
        <select id="subject" v-model="filters.subjectId" class="select" style="width: 150px">
          <option value="">全部科目</option>
          <option v-for="item in mySubjects" :key="item.subjectId" :value="item.subjectId">
            {{ item.name }}
          </option>
        </select>
      </div>
      <button type="button" class="btn btn-primary" @click="onSearch">查询</button>
      <button type="button" class="btn" @click="onReset">重置</button>
      <div class="spacer"></div>
      <span class="hint">共 {{ data.scoredSessionList.length }} 次月考 · 6 门科目 · 高考总分 {{ data.gradeInfo.totalFullScore }}</span>
    </FilterBar>

    <!-- 最近一次月考汇总 -->
    <div v-if="latestSummary" class="card stat-bar mt-16">
      <div class="stat-item">
        <span class="stat-value">{{ latestSummary.total }}</span>
        <span class="stat-label">{{ latestSummary.name }} 总分</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ (latestSummary.rate * 100).toFixed(1) }}%</span>
        <span class="stat-label">得分率</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">第 {{ latestSummary.classRank }} 名</span>
        <span class="stat-label">班级排名（共 {{ data.studentList.length }} 人）</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">第 {{ latestSummary.gradeRank }} 名</span>
        <span class="stat-label">年级排名（共 {{ data.gradeInfo.gradeStudentCount }} 人）</span>
      </div>
      <div class="stat-item">
        <span class="stat-value" :style="{ color: latestLevel?.color }">{{ latestLevel?.label }}</span>
        <span class="stat-label">等级</span>
      </div>
    </div>

    <!-- 成绩单矩阵 -->
    <div class="card card-flush mt-16">
      <div class="card-head">
        <h3 class="card-title" style="margin: 0">成绩单（科目 × 月考场次）</h3>
        <span class="hint">单元格为分数，颜色按得分率</span>
      </div>

      <template v-if="matrix.rows.length">
        <DataTable :columns="matrixColumns" :rows="matrix.rows" row-key="subjectId">
          <template #cell="{ row, column }">
            <template v-if="column.key === 'name'">
              <b>{{ row.name }}</b>
            </template>
            <template v-else-if="column.key === 'fullScore'">
              <span class="text-secondary">{{ row.fullScore }}</span>
            </template>
            <template v-else-if="column.key === 'delta'">
              <span :style="{ color: deltaColor(row.delta), fontWeight: 600 }">{{ deltaText(row.delta) }}</span>
              <span class="hint"> 分</span>
            </template>
            <template v-else-if="column.key.startsWith('exam-')">
              <span
                v-if="cellOf(row, column.key.slice(5)).score === null"
                class="text-secondary"
              >—</span>
              <span
                v-else
                class="score"
                :style="{ color: rateColor(cellOf(row, column.key.slice(5)).rate) }"
              >
                {{ cellOf(row, column.key.slice(5)).score }}
              </span>
            </template>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
      </template>
      <div v-else class="empty-wrap">
        <EmptyState title="暂无成绩数据" desc="当前筛选条件下没有查到月考成绩，试试切换场次或科目" />
      </div>
    </div>

    <!-- 总分与排名 -->
    <div class="card card-flush section-gap">
      <div class="card-head">
        <h3 class="card-title" style="margin: 0">总分与排名</h3>
        <span class="hint">年级排名为演示环境的估算值（按全年级总分正态分布折算）</span>
      </div>
      <DataTable :columns="rankColumns" :rows="rankRows" row-key="examId">
        <template #cell="{ row, column }">
          <template v-if="column.key === 'total'">
            <span v-if="row.total === null" class="text-secondary">—</span>
            <b v-else :style="{ color: rateColor(row.rate) }">{{ row.total }}</b>
          </template>
          <template v-else-if="column.key === 'rate'">
            <span v-if="row.rate === null" class="text-secondary">—</span>
            <span v-else>{{ (row.rate * 100).toFixed(1) }}%</span>
          </template>
          <template v-else-if="column.key === 'classRank'">
            <StatusTag v-if="row.classRank" value="NORMAL" :label="`第 ${row.classRank} 名`" />
            <span v-else>—</span>
          </template>
          <template v-else-if="column.key === 'gradeRank'">
            <StatusTag v-if="row.gradeRank" value="MODIFIED" :label="`第 ${row.gradeRank} 名`" />
            <span v-else>—</span>
          </template>
          <template v-else-if="column.key === 'level'">
            <span v-if="!row.level">—</span>
            <span v-else class="level" :style="{ color: gradeLevel(row.rate).color, background: gradeLevel(row.rate).bg, borderColor: gradeLevel(row.rate).border }">
              {{ row.level }}
            </span>
          </template>
          <template v-else>{{ row[column.key] ?? '—' }}</template>
        </template>
      </DataTable>
    </div>

    <p class="hint mt-12">
      新高考「3+1+2」：语文、数学、外语各 150 分，物理/历史二选一 100 分，化学、生物、思想政治、地理四选二各 100 分，总分 750 分。
      等级按得分率划分：优秀 ≥85%、良好 ≥70%、及格 ≥60%。
    </p>
  </div>
</template>

<style scoped>
.student-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  margin-bottom: 16px;
}

.who {
  font-size: var(--font-size-section);
  font-weight: 700;
}

.meta {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.chip {
  font-size: 12px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  border: 1px solid #d3e0f0;
  border-radius: 10px;
  padding: 1px 10px;
}

.score {
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.level {
  display: inline-block;
  font-size: 12px;
  line-height: 20px;
  padding: 0 9px;
  border-radius: 10px;
  border: 1px solid;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 16px 12px;
}

.empty-wrap {
  padding: 0 16px 16px;
}
</style>
