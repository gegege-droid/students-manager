<script setup>
import { computed, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { gradeLevel, rateColor } from '@/utils/score'
import { validateChangeReason } from '@/utils/validate'

/** P6 修改成绩 —— 按「学生 + 月考场次 + 科目」定位记录，改分并留痕 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const query = reactive({ keyword: '', examId: '', subjectId: '' })
const queryError = ref('')
const searched = ref(false)
const matches = ref([])
const target = ref(null)
const newScore = ref('')
const reason = ref('')
const errors = reactive({ score: '', reason: '' })
const confirmVisible = ref(false)

const locked = computed(() => target.value?.status === 'APPEALING')
const maxScore = computed(() => target.value?.fullScore || 100)

const diff = computed(() => {
  if (!target.value) return null
  const value = Number(newScore.value)
  if (newScore.value === '' || Number.isNaN(value)) return null
  return value - target.value.score
})

const matchColumns = [
  { key: 'studentId', title: '学号', width: '130px' },
  { key: 'studentName', title: '姓名', width: '110px' },
  { key: 'examName', title: '月考场次', width: '150px' },
  { key: 'subjectName', title: '科目', width: '120px' },
  { key: 'score', title: '分数', width: '100px', align: 'center' },
  { key: 'status', title: '状态', width: '120px', align: 'center' },
  { key: 'action', title: '操作', width: '100px', align: 'center' }
]

const logColumns = [
  { key: 'operateTime', title: '修改时间', width: '150px', nowrap: true },
  { key: 'operator', title: '操作人', width: '110px' },
  { key: 'examName', title: '月考场次', width: '130px' },
  { key: 'subjectName', title: '科目', width: '110px' },
  { key: 'changeText', title: '改动内容', width: '180px' },
  { key: 'changeReason', title: '修改原因', width: '330px' },
  { key: 'source', title: '来源', width: '130px', align: 'center' }
]

const logs = computed(() =>
  target.value ? data.logsOf(target.value.studentId, target.value.examId, target.value.subjectId) : []
)

function onSearch() {
  queryError.value = ''
  target.value = null
  matches.value = []

  const keyword = query.keyword.trim()
  if (!keyword && !query.examId && !query.subjectId) {
    searched.value = false
    queryError.value = '请至少输入一个查询条件（学号 / 姓名 / 月考场次 / 科目）'
    return
  }

  matches.value = data.findScores({
    keyword,
    examId: query.examId,
    subjectId: query.subjectId,
    classId: user.manageClassIds?.[0] || ''
  })
  searched.value = true
  if (matches.value.length === 1) loadTarget(matches.value[0])
}

function loadTarget(row) {
  target.value = row
  newScore.value = String(row.score ?? '')
  reason.value = ''
  errors.score = ''
  errors.reason = ''
}

function onClear() {
  query.keyword = ''
  query.examId = ''
  query.subjectId = ''
  queryError.value = ''
  searched.value = false
  matches.value = []
  target.value = null
}

function onCancelEdit() {
  if (!target.value) return
  newScore.value = String(target.value.score ?? '')
  reason.value = ''
  errors.score = ''
  errors.reason = ''
}

function onSave() {
  if (!target.value || locked.value) return
  errors.score = ''
  errors.reason = ''

  const value = Number(newScore.value)
  if (newScore.value === '' || Number.isNaN(value) || value < 0 || value > maxScore.value) {
    errors.score = `分数必须是 0~${maxScore.value} 的整数`
  } else if (value === target.value.score) {
    errors.score = '新分数与原分数相同，无需修改'
  }
  errors.reason = validateChangeReason(reason.value)
  if (errors.score || errors.reason) return
  confirmVisible.value = true
}

function onConfirmSave() {
  const { studentId, examId, subjectId } = target.value
  const result = data.updateScore({
    studentId,
    examId,
    subjectId,
    newScore: Number(newScore.value),
    reason: reason.value,
    operatorId: user.loginId
  })
  confirmVisible.value = false

  if (!result) {
    toast.error('保存失败，未找到对应成绩记录')
    return
  }
  toast.success('修改成功，已生成修改记录')
  onSearch()
  const found = matches.value.find(
    (row) => row.studentId === studentId && row.examId === examId && row.subjectId === subjectId
  )
  if (found) loadTarget(found)
}

function operatorName(loginId) {
  return data.userNameOf(loginId)
}

function changeText(log) {
  const delta = log.newScore - log.oldScore
  return `${log.oldScore} → ${log.newScore}（${delta > 0 ? '+' : ''}${delta}）`
}

const confirmText = computed(() => {
  if (!target.value || diff.value === null) return ''
  return `${target.value.subjectName}：${target.value.score} → ${newScore.value}（${diff.value > 0 ? '+' : ''}${diff.value} 分）`
})
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">修改成绩</h2>
      <span class="breadcrumb">首页 / 修改成绩</span>
    </div>

    <div class="card filter-bar">
      <div class="filter-item">
        <label class="label" for="keyword">学号/姓名</label>
        <input
          id="keyword"
          v-model="query.keyword"
          class="input"
          style="width: 170px"
          type="text"
          placeholder="如 20230101 或 林晓"
          @keyup.enter="onSearch"
        />
      </div>
      <div class="filter-item">
        <label class="label" for="exam">月考场次</label>
        <select id="exam" v-model="query.examId" class="select" style="width: 160px">
          <option value="">全部场次</option>
          <option v-for="item in data.scoredSessionList" :key="item.examId" :value="item.examId">
            {{ item.name }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="subject">科目</label>
        <select id="subject" v-model="query.subjectId" class="select" style="width: 150px">
          <option value="">全部科目</option>
          <option v-for="item in data.subjectList" :key="item.subjectId" :value="item.subjectId">
            {{ item.name }}
          </option>
        </select>
      </div>
      <button type="button" class="btn btn-primary" @click="onSearch">查询</button>
      <button type="button" class="btn" @click="onClear">清空</button>
    </div>

    <div v-if="queryError" class="alert alert-warning mt-16">⚠ {{ queryError }}</div>

    <!-- 多条命中时点选 -->
    <div v-if="matches.length > 1 && !target" class="card card-flush mt-16">
      <div class="list-head">
        <h3 class="card-title" style="margin: 0">匹配到 {{ matches.length }} 条成绩记录</h3>
        <span class="hint">点击「修改」载入要更正的记录</span>
      </div>
      <DataTable :columns="matchColumns" :rows="matches" row-key="scoreId">
        <template #cell="{ row, column }">
          <template v-if="column.key === 'score'">
            <b :style="{ color: rateColor(row.score / row.fullScore) }">{{ row.score }}</b>
            <span class="hint"> / {{ row.fullScore }}</span>
          </template>
          <StatusTag v-else-if="column.key === 'status'" :value="row.status" />
          <button v-else-if="column.key === 'action'" type="button" class="btn btn-ghost btn-sm" @click="loadTarget(row)">
            修改
          </button>
          <template v-else>{{ row[column.key] ?? '—' }}</template>
        </template>
      </DataTable>
    </div>

    <div v-if="target && locked" class="alert alert-warning mt-16">
      ⚠ 该成绩存在待受理的查分申请，请先到「受理查分申请」完成受理后再修改。
    </div>

    <!-- 修改区 -->
    <div v-if="target" class="card mt-16">
      <div class="row-between mb-12">
        <b>
          {{ target.studentId }}　{{ target.studentName }}　{{ target.className }}　·　{{ target.combination }}
        </b>
        <StatusTag :value="target.status" />
      </div>

      <table class="table compare-table">
        <thead>
          <tr>
            <th style="width: 160px">月考场次 · 科目</th>
            <th class="c" style="width: 200px">原分数（只读）</th>
            <th class="c" style="width: 200px">新分数（可编辑）</th>
            <th>说明</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{{ target.examName }} · {{ target.subjectName }}</td>
            <td class="c">
              <span class="origin">{{ target.score }} / {{ target.fullScore }}</span>
            </td>
            <td class="c">
              <input
                v-model="newScore"
                class="input input-sm"
                style="width: 110px; text-align: center"
                type="text"
                inputmode="numeric"
                :disabled="locked"
              />
            </td>
            <td class="hint">
              <template v-if="diff !== null">
                改动：{{ target.score }} → {{ newScore }}（{{ diff > 0 ? '+' : '' }}{{ diff }} 分）
                · 等级 {{ gradeLevel(target.score / target.fullScore).label }} →
                {{ gradeLevel(Number(newScore) / target.fullScore).label }}
              </template>
              <template v-else>请输入新分数</template>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="field mt-16">
        <label class="label" for="reason">修改原因 <span class="req">*</span>（5~100 字）</label>
        <textarea
          id="reason"
          v-model="reason"
          class="textarea"
          :class="{ 'is-error': errors.reason }"
          rows="3"
          maxlength="100"
          :disabled="locked"
          placeholder="例如：登分时看错行，据实更正"
        ></textarea>
        <span v-if="errors.reason" class="err">{{ errors.reason }}</span>
      </div>

      <span v-if="errors.score" class="err mb-12">{{ errors.score }}</span>

      <div class="btn-group mt-12">
        <button type="button" class="btn btn-primary" :disabled="locked" @click="onSave">保存修改</button>
        <button type="button" class="btn" :disabled="locked" @click="onCancelEdit">取消修改</button>
        <button v-if="matches.length > 1" type="button" class="btn" @click="target = null">重新选择成绩</button>
        <span class="hint">保存前会弹出二次确认，原分数将保留在修改记录中</span>
      </div>
    </div>

    <!-- 修改记录 -->
    <div v-if="target" class="card card-flush section-gap">
      <div class="list-head">
        <h3 class="card-title" style="margin: 0">修改记录</h3>
        <span class="hint">共 {{ logs.length }} 条（最新在上）</span>
      </div>
      <template v-if="logs.length">
        <DataTable :columns="logColumns" :rows="logs" row-key="logId">
          <template #cell="{ row, column }">
            <span v-if="column.key === 'operator'">{{ operatorName(row.operatorId) }}</span>
            <StatusTag v-else-if="column.key === 'source'" :value="row.source" />
            <span v-else-if="column.key === 'changeText'">{{ changeText(row) }}</span>
            <span v-else-if="column.key === 'changeReason'" class="ellipsis" :title="row.changeReason">
              {{ row.changeReason }}
            </span>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
      </template>
      <div v-else class="empty-wrap">
        <EmptyState title="暂无修改记录" desc="该成绩自录入以来没有被修改过" />
      </div>
    </div>

    <!-- 未查询 / 无结果 -->
    <div v-if="!target && !matches.length" class="card card-flush section-gap">
      <div class="empty-wrap">
        <EmptyState
          v-if="searched && !queryError"
          title="未查询到成绩记录"
          desc="没有找到符合条件的成绩，请检查学号 / 姓名，或更换月考场次与科目"
        />
        <EmptyState
          v-else
          title="请输入条件查询成绩"
          desc="支持按学号、姓名、月考场次、科目组合查询；查询到记录后可修改分数并留痕"
        />
      </div>
    </div>

    <BaseModal
      v-model="confirmVisible"
      title="确认修改成绩？"
      :width="460"
      confirm-text="确认修改"
      @confirm="onConfirmSave"
    >
      <p>本次改动：{{ confirmText || '—' }}</p>
      <p class="hint mt-8">修改后原分数将保留在修改记录中，操作人与操作时间会被记录，无法删除。</p>
    </BaseModal>
  </div>
</template>

<style scoped>
.compare-table td {
  vertical-align: middle;
}

.origin {
  display: inline-block;
  min-width: 90px;
  padding: 4px 10px;
  background: #f7fafc;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  color: #4a5568;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
}

.empty-wrap {
  padding: 16px;
}
</style>
