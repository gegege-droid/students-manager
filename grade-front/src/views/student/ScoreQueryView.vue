<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterBar from '@/components/FilterBar.vue'
import Pagination from '@/components/Pagination.vue'
import ScoreCell from '@/components/ScoreCell.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { calcAvgGpa, calcAvgScore } from '@/utils/score'

/** P2 成绩查询 —— 对应 FR-P2-01 ~ FR-P2-10 */

const user = useUserStore()
const data = useScoreStore()
const router = useRouter()

const PAGE_SIZE = 10

const filters = reactive({ term: '2024-2025-1', keyword: '' })
/** 「查询」按钮点击后才生效的条件（学期下拉是即时的） */
const applied = reactive({ term: '2024-2025-1', keyword: '' })
const page = ref(1)

const rows = computed(() => data.scoreRowsOfStudent(user.loginId, applied))

const stats = computed(() => ({
  count: rows.value.length,
  credit: rows.value.reduce((sum, row) => sum + (row.credit || 0), 0),
  avgScore: calcAvgScore(rows.value),
  avgGpa: calcAvgGpa(rows.value)
}))

const pagedRows = computed(() => rows.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

const columns = [
  { key: 'index', title: '序号', width: '64px', align: 'center' },
  { key: 'courseId', title: '课程号', width: '100px' },
  { key: 'courseName', title: '课程名称', width: '200px' },
  { key: 'credit', title: '学分', width: '70px', align: 'center' },
  { key: 'usualScore', title: '平时成绩', width: '100px', align: 'center' },
  { key: 'finalScore', title: '期末成绩', width: '100px', align: 'center' },
  { key: 'totalScore', title: '总评成绩', width: '120px', align: 'center' },
  { key: 'gpa', title: '绩点', width: '80px', align: 'center' },
  { key: 'status', title: '状态', width: '96px', align: 'center' },
  { key: 'action', title: '操作', width: '110px', align: 'center' }
]

watch(
  () => filters.term,
  () => {
    applied.term = filters.term
    page.value = 1
  }
)

function onSearch() {
  applied.keyword = filters.keyword.trim()
  page.value = 1
}

function onReset() {
  filters.term = '2024-2025-1'
  filters.keyword = ''
  applied.term = filters.term
  applied.keyword = ''
  page.value = 1
}

/** FR-P2-09：把课程号带给 P3 */
function goAppeal(row) {
  router.push({ name: 'StudentAppealApply', query: { courseId: row.courseId } })
}

const emptyTitle = computed(() => (applied.keyword ? '没有找到符合条件的成绩' : '暂无成绩数据'))
const emptyDesc = computed(() =>
  applied.keyword
    ? `没有找到包含「${applied.keyword}」的课程，请更换关键字或清空筛选条件`
    : '当前学期还没有录入成绩，试试切换学期或清空筛选条件'
)

/** 绩点统一保留 1 位小数（3 → 3.0） */
function gpaText(value) {
  return value === null || value === undefined ? '—' : Number(value).toFixed(1)
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">成绩查询</h2>
      <span class="breadcrumb">首页 / 成绩查询</span>
    </div>

    <!-- ② 筛选区 -->
    <FilterBar>
      <div class="filter-item">
        <label class="label" for="term">学期</label>
        <select id="term" v-model="filters.term" class="select" style="width: 180px">
          <option value="">全部学期</option>
          <option v-for="item in data.termOptions" :key="item" :value="item">{{ item }}</option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="keyword">课程</label>
        <input
          id="keyword"
          v-model="filters.keyword"
          class="input"
          style="width: 240px"
          type="text"
          placeholder="课程名称 / 课程号"
          @keyup.enter="onSearch"
        />
      </div>
      <button type="button" class="btn btn-primary" @click="onSearch">查询</button>
      <button type="button" class="btn" @click="onReset">重置</button>
    </FilterBar>

    <!-- ③ 统计条 -->
    <div v-if="rows.length" class="card stat-bar mt-16">
      <div class="stat-item">
        <span class="stat-value">{{ stats.count }}</span>
        <span class="stat-label">已修课程</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.credit }}</span>
        <span class="stat-label">总学分</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.avgScore }}</span>
        <span class="stat-label">平均分</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.avgGpa }}</span>
        <span class="stat-label">平均绩点</span>
      </div>
    </div>

    <!-- ④ 数据区 -->
    <div class="card card-flush mt-16">
      <template v-if="rows.length">
        <DataTable :columns="columns" :rows="pagedRows" row-key="scoreId">
          <template #cell="{ row, column, index }">
            <template v-if="column.key === 'index'">{{ (page - 1) * PAGE_SIZE + index + 1 }}</template>
            <ScoreCell v-else-if="column.key === 'totalScore'" :value="row.totalScore" show-level />
            <StatusTag v-else-if="column.key === 'status'" :value="row.status" />
            <span v-else-if="column.key === 'gpa'">{{ gpaText(row.gpa) }}</span>
            <template v-else-if="column.key === 'action'">
              <button
                type="button"
                class="btn btn-ghost btn-sm"
                :disabled="row.status === 'APPEALING'"
                :title="row.status === 'APPEALING' ? '该课程已有待受理的查分申请' : '对该课程发起查分申请'"
                @click="goAppeal(row)"
              >
                申请查分
              </button>
            </template>
            <span v-else-if="column.key === 'credit'">{{ row.credit }}</span>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>

        <Pagination v-model:page="page" :total="rows.length" :page-size="PAGE_SIZE" />
      </template>

      <div v-else class="empty-wrap">
        <EmptyState :title="emptyTitle" :desc="emptyDesc">
          <button type="button" class="btn" @click="onReset">清空筛选条件</button>
        </EmptyState>
      </div>
    </div>

    <p class="hint mt-12">
      总评 = 平时成绩 × 40% + 期末成绩 × 60%，由系统自动计算；低于 60 分以红色显示并标注「不及格」。
    </p>
  </div>
</template>

<style scoped>
.empty-wrap {
  padding: 16px;
}
</style>
