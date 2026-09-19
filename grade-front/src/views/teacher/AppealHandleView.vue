<script setup>
import { computed, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { rateColor } from '@/utils/score'
import { validateOpinion } from '@/utils/validate'

/** P7 受理查分申请 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const activeTab = ref('PENDING')
const filters = reactive({ keyword: '', examId: '', subjectId: '', timeRange: '' })

const pendingRows = computed(() => data.appealRowsByStatus('PENDING'))
const handledRows = computed(() => data.appealRowsByStatus('HANDLED'))

function inTimeRange(applyTime) {
  if (!filters.timeRange) return true
  const days = Number(filters.timeRange)
  const time = new Date(String(applyTime).replace(/-/g, '/')).getTime()
  if (Number.isNaN(time)) return true
  return Date.now() - time <= days * 24 * 60 * 60 * 1000
}

const rows = computed(() => {
  const base = activeTab.value === 'PENDING' ? pendingRows.value : handledRows.value
  const keyword = filters.keyword.trim()
  return base
    .filter((row) => {
      if (filters.examId && row.examId !== filters.examId) return false
      if (filters.subjectId && row.subjectId !== filters.subjectId) return false
      if (keyword) {
        return (
          row.appealId.includes(keyword) ||
          row.studentId.includes(keyword) ||
          row.studentName.includes(keyword)
        )
      }
      return true
    })
    .filter((row) => inTimeRange(row.applyTime))
})

const columns = computed(() => {
  const base = [
    { key: 'appealId', title: '申请编号', width: '140px' },
    { key: 'studentId', title: '学号', width: '110px' },
    { key: 'studentName', title: '姓名', width: '90px' },
    { key: 'examName', title: '月考场次', width: '120px' },
    { key: 'subjectName', title: '科目', width: '100px' },
    { key: 'originalScore', title: '原分数', width: '90px', align: 'center' },
    { key: 'applyTime', title: '申请时间', width: '140px', nowrap: true }
  ]
  if (activeTab.value === 'PENDING') {
    return [
      ...base,
      { key: 'reason', title: '申请理由', width: '200px' },
      { key: 'action', title: '操作', width: '162px', align: 'center' }
    ]
  }
  return [
    { key: 'appealId', title: '申请编号', width: '140px' },
    { key: 'studentId', title: '学号', width: '100px' },
    { key: 'studentName', title: '姓名', width: '90px' },
    { key: 'examName', title: '月考场次', width: '120px' },
    { key: 'subjectName', title: '科目', width: '100px' },
    { key: 'originalScore', title: '原分数', width: '90px', align: 'center' },
    { key: 'applyTime', title: '申请时间', width: '130px', nowrap: true },
    { key: 'result', title: '处理结果', width: '120px', align: 'center' },
    { key: 'handleTime', title: '处理时间', width: '130px', nowrap: true },
    { key: 'action', title: '操作', width: '90px', align: 'center' }
  ]
})

// ==================== 弹窗 ====================

const modalVisible = ref(false)
const modalMode = ref('handle') // handle | reject | detail
const current = ref(null)
const form = reactive({ result: 'KEEP', correctedScore: '', opinion: '' })
const errors = reactive({ result: '', correctedScore: '', opinion: '' })

function resetErrors() {
  errors.result = ''
  errors.correctedScore = ''
  errors.opinion = ''
}

function closeModal() {
  modalVisible.value = false
  current.value = null
  resetErrors()
}

function openHandle(row) {
  modalMode.value = 'handle'
  current.value = row
  form.result = 'KEEP'
  form.correctedScore = ''
  form.opinion = ''
  resetErrors()
  modalVisible.value = true
}

function openReject(row) {
  modalMode.value = 'reject'
  current.value = row
  form.opinion = ''
  resetErrors()
  modalVisible.value = true
}

function openDetail(row) {
  modalMode.value = 'detail'
  current.value = row
  modalVisible.value = true
}

const modalTitle = computed(() => {
  if (!current.value) return ''
  if (modalMode.value === 'reject') return `驳回查分申请 · ${current.value.appealId}`
  if (modalMode.value === 'detail') return `查分申请详情 · ${current.value.appealId}`
  return `受理查分申请 · ${current.value.appealId}`
})

function onConfirm() {
  if (!current.value) return

  if (modalMode.value === 'detail') {
    closeModal()
    return
  }

  if (modalMode.value === 'reject') {
    errors.opinion = validateOpinion(form.opinion)
    if (errors.opinion) return
    data.rejectAppeal(current.value.appealId, { opinion: form.opinion, handlerId: user.loginId })
    closeModal()
    toast.success('已驳回该查分申请')
    return
  }

  errors.result = form.result ? '' : '请选择处理结果'
  const corrected = Number(form.correctedScore)
  errors.correctedScore =
    form.result === 'CORRECT' &&
    (form.correctedScore === '' ||
      Number.isNaN(corrected) ||
      corrected < 0 ||
      corrected > current.value.fullScore)
      ? `请填写更正后的分数（0~${current.value.fullScore}）`
      : ''
  errors.opinion = validateOpinion(form.opinion)
  if (errors.result || errors.correctedScore || errors.opinion) return

  data.handleAppeal(current.value.appealId, {
    result: form.result,
    correctedScore: form.result === 'CORRECT' ? corrected : null,
    opinion: form.opinion,
    handlerId: user.loginId
  })
  closeModal()
  toast.success('受理完成，处理意见已记录')
}

function onResetFilters() {
  filters.keyword = ''
  filters.examId = ''
  filters.subjectId = ''
  filters.timeRange = ''
}

function operatorName(loginId) {
  return data.userNameOf(loginId)
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">受理查分申请</h2>
      <span class="breadcrumb">首页 / 受理查分申请</span>
    </div>

    <div class="tabs">
      <button type="button" class="tab" :class="{ 'is-active': activeTab === 'PENDING' }" @click="activeTab = 'PENDING'">
        待受理 ({{ pendingRows.length }})
      </button>
      <button type="button" class="tab" :class="{ 'is-active': activeTab === 'HANDLED' }" @click="activeTab = 'HANDLED'">
        已受理 ({{ handledRows.length }})
      </button>
    </div>

    <div class="card filter-bar">
      <div class="filter-item">
        <input v-model="filters.keyword" class="input" style="width: 190px" type="text" placeholder="申请编号 / 学号 / 姓名" />
      </div>
      <div class="filter-item">
        <select v-model="filters.examId" class="select" style="width: 160px">
          <option value="">全部场次</option>
          <option v-for="item in data.scoredSessionList" :key="item.examId" :value="item.examId">{{ item.name }}</option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filters.subjectId" class="select" style="width: 150px">
          <option value="">全部科目</option>
          <option v-for="item in data.subjectList" :key="item.subjectId" :value="item.subjectId">{{ item.name }}</option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filters.timeRange" class="select" style="width: 130px">
          <option value="">全部时间</option>
          <option value="1">今天</option>
          <option value="7">近 7 天</option>
          <option value="30">近 30 天</option>
        </select>
      </div>
      <button type="button" class="btn" @click="onResetFilters">重置</button>
      <div class="spacer"></div>
      <span class="hint">学生提交后实时出现在「待受理」</span>
    </div>

    <div class="card card-flush section-gap">
      <template v-if="rows.length">
        <DataTable :columns="columns" :rows="rows" row-key="appealId">
          <template #cell="{ row, column }">
            <template v-if="column.key === 'action'">
              <template v-if="activeTab === 'PENDING'">
                <button type="button" class="btn btn-primary btn-sm" @click="openHandle(row)">受理</button>
                <button type="button" class="btn btn-ghost btn-sm" @click="openReject(row)">驳回</button>
              </template>
              <button v-else type="button" class="btn btn-ghost btn-sm" @click="openDetail(row)">查看</button>
            </template>
            <StatusTag
              v-else-if="column.key === 'result'"
              :value="row.status === 'REJECTED' ? 'REJECTED' : row.result"
            />
            <template v-else-if="column.key === 'originalScore'">
              <b :style="{ color: rateColor(row.originalScore / row.fullScore) }">{{ row.originalScore }}</b>
              <span class="hint"> / {{ row.fullScore }}</span>
            </template>
            <span v-else-if="column.key === 'reason'" class="ellipsis" :title="row.reason">{{ row.reason }}</span>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
        <div class="pagination">共 {{ rows.length }} 条</div>
      </template>

      <div v-else class="empty-wrap">
        <EmptyState
          v-if="activeTab === 'PENDING'"
          title="暂无待受理的查分申请"
          desc="学生提交查分申请后会出现在这里"
        />
        <EmptyState v-else title="暂无已受理的查分申请" desc="受理或驳回后的申请会出现在这里" />
      </div>
    </div>

    <BaseModal
      v-model="modalVisible"
      :title="modalTitle"
      :width="560"
      :show-footer="modalMode !== 'detail'"
      :confirm-text="modalMode === 'reject' ? '确认驳回' : '确认受理'"
      @confirm="onConfirm"
      @cancel="closeModal"
    >
      <div v-if="current">
        <div class="modal-row">
          <span class="modal-label">学生</span>
          <span>{{ current.studentId }}　{{ current.studentName }}　{{ current.className }}</span>
        </div>
        <div class="modal-row">
          <span class="modal-label">场次科目</span>
          <span>{{ current.examName }} · {{ current.subjectName }}（满分 {{ current.fullScore }}）</span>
        </div>
        <div class="modal-row">
          <span class="modal-label">原分数</span>
          <span>
            <b :style="{ color: rateColor(current.originalScore / current.fullScore) }">{{ current.originalScore }}</b>
            / {{ current.fullScore }}
            · 等级 {{ (current.originalScore / current.fullScore * 100).toFixed(1) }}% 得分率
          </span>
        </div>
        <div class="modal-row">
          <span class="modal-label">申请时间</span>
          <span>{{ current.applyTime }}</span>
        </div>
        <div class="modal-row">
          <span class="modal-label">申请理由</span>
          <span class="reason-box">{{ current.reason }}</span>
        </div>

        <template v-if="modalMode === 'handle'">
          <div class="field mt-16">
            <span class="label">处理结果 <span class="req">*</span></span>
            <div class="radio-group">
              <label class="radio"><input v-model="form.result" type="radio" value="KEEP" /><span>维持原成绩</span></label>
              <label class="radio"><input v-model="form.result" type="radio" value="CORRECT" /><span>更正成绩</span></label>
            </div>
            <span v-if="errors.result" class="err">{{ errors.result }}</span>
          </div>

          <div v-if="form.result === 'CORRECT'" class="field">
            <label class="label" for="corrected">更正后分数 <span class="req">*</span></label>
            <input
              id="corrected"
              v-model="form.correctedScore"
              class="input"
              :class="{ 'is-error': errors.correctedScore }"
              style="width: 140px"
              type="text"
              inputmode="numeric"
              :placeholder="`0~${current.fullScore}`"
            />
            <span v-if="errors.correctedScore" class="err">{{ errors.correctedScore }}</span>
            <span v-else class="hint">更正后会同步更新该次月考该科成绩，并写入修改记录</span>
          </div>

          <div class="field">
            <label class="label" for="opinion">处理意见 <span class="req">*</span>（5~200 字）</label>
            <textarea
              id="opinion"
              v-model="form.opinion"
              class="textarea"
              :class="{ 'is-error': errors.opinion }"
              rows="3"
              maxlength="200"
              placeholder="例如：经复核，该题步骤分漏加，已更正为 xx 分"
            ></textarea>
            <span v-if="errors.opinion" class="err">{{ errors.opinion }}</span>
          </div>
        </template>

        <template v-else-if="modalMode === 'reject'">
          <div class="field mt-16">
            <label class="label" for="reject-opinion">驳回理由 <span class="req">*</span>（5~200 字）</label>
            <textarea
              id="reject-opinion"
              v-model="form.opinion"
              class="textarea"
              :class="{ 'is-error': errors.opinion }"
              rows="3"
              maxlength="200"
              placeholder="例如：已调阅原卷逐题复核，计分无误，驳回申请"
            ></textarea>
            <span v-if="errors.opinion" class="err">{{ errors.opinion }}</span>
          </div>
        </template>

        <template v-else>
          <div class="modal-row"><span class="modal-label">处理状态</span><StatusTag :value="current.status" /></div>
          <div v-if="current.result" class="modal-row">
            <span class="modal-label">处理结果</span><StatusTag :value="current.result" />
          </div>
          <div v-if="current.correctedScore !== null" class="modal-row">
            <span class="modal-label">更正后分数</span><b class="text-success">{{ current.correctedScore }}</b>
          </div>
          <div class="modal-row"><span class="modal-label">处理意见</span><span>{{ current.opinion || '—' }}</span></div>
          <div class="modal-row"><span class="modal-label">受理人</span><span>{{ operatorName(current.handlerId) }}</span></div>
          <div class="modal-row"><span class="modal-label">处理时间</span><span>{{ current.handleTime || '—' }}</span></div>
        </template>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
.empty-wrap {
  padding: 16px;
}

.modal-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed #edf0f4;
  font-size: 13px;
}

.modal-label {
  flex: 0 0 84px;
  color: var(--color-text-secondary);
}

.reason-box {
  flex: 1;
  background: #f7fafc;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  padding: 8px 12px;
}

.radio-group {
  display: flex;
  gap: 24px;
  font-size: 13px;
}

.radio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.text-success {
  color: var(--color-success);
}
</style>
