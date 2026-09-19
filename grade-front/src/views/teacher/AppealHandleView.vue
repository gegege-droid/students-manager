<script setup>
import { computed, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { isScoreValue, validateOpinion } from '@/utils/validate'
import { scoreColor } from '@/utils/score'

/** P6 受理查分申请 —— 对应 FR-P6-01 ~ FR-P6-10 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const activeTab = ref('PENDING')
const filters = reactive({ keyword: '', courseId: '', timeRange: '' })

const pendingRows = computed(() => data.appealRowsByStatus('PENDING'))
const handledRows = computed(() => data.appealRowsByStatus('HANDLED'))

const rows = computed(() => {
  const base = activeTab.value === 'PENDING' ? pendingRows.value : handledRows.value
  const keyword = filters.keyword.trim()
  return base
    .filter((row) => {
      if (filters.courseId && row.courseId !== filters.courseId) return false
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

function inTimeRange(applyTime) {
  if (!filters.timeRange) return true
  const days = Number(filters.timeRange)
  const time = new Date(String(applyTime).replace(/-/g, '/')).getTime()
  if (Number.isNaN(time)) return true
  return Date.now() - time <= days * 24 * 60 * 60 * 1000
}

// ==================== 弹窗 ====================

const modalVisible = ref(false)
const modalMode = ref('handle') // handle | reject | detail
const current = ref(null)
const form = reactive({ result: 'KEEP', correctedScore: '', opinion: '' })
const errors = reactive({ result: '', correctedScore: '', opinion: '' })

const columns = computed(() => {
  const base = [
    { key: 'appealId', title: '申请编号', width: '150px' },
    { key: 'studentId', title: '学号', width: '110px' },
    { key: 'studentName', title: '姓名', width: '90px' },
    { key: 'courseName', title: '课程名称', width: '160px' },
    { key: 'originalScore', title: '原成绩', width: '90px', align: 'center' },
    { key: 'applyTime', title: '申请时间', width: '150px', nowrap: true }
  ]
  if (activeTab.value === 'PENDING') {
    return [
      ...base,
      { key: 'reason', title: '申请理由', width: '240px' },
      { key: 'action', title: '操作', width: '170px', align: 'center' }
    ]
  }
  return [
    ...base,
    { key: 'result', title: '处理结果', width: '130px', align: 'center' },
    { key: 'handleTime', title: '处理时间', width: '150px', nowrap: true },
    { key: 'action', title: '操作', width: '100px', align: 'center' }
  ]
})

function closeModal() {
  modalVisible.value = false
  current.value = null
  errors.result = ''
  errors.correctedScore = ''
  errors.opinion = ''
}

/** FR-P6-03：打开受理弹窗并带入该条申请详情 */
function openHandle(row) {
  modalMode.value = 'handle'
  current.value = row
  form.result = 'KEEP'
  form.correctedScore = ''
  form.opinion = ''
  errors.result = ''
  errors.correctedScore = ''
  errors.opinion = ''
  modalVisible.value = true
}

function openReject(row) {
  modalMode.value = 'reject'
  current.value = row
  form.opinion = ''
  errors.opinion = ''
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

  // 受理
  errors.result = form.result ? '' : '请选择处理结果'
  errors.correctedScore =
    form.result === 'CORRECT' && !isScoreValue(form.correctedScore)
      ? '请填写更正后的成绩（0~100）'
      : ''
  errors.opinion = validateOpinion(form.opinion)
  if (errors.result || errors.correctedScore || errors.opinion) return

  data.handleAppeal(current.value.appealId, {
    result: form.result,
    correctedScore: form.result === 'CORRECT' ? Number(form.correctedScore) : null,
    opinion: form.opinion,
    handlerId: user.loginId
  })
  closeModal()
  toast.success('受理完成，处理意见已记录')
}

function onReset() {
  filters.keyword = ''
  filters.courseId = ''
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

    <!-- ② 标签页 -->
    <div class="tabs">
      <button
        type="button"
        class="tab"
        :class="{ 'is-active': activeTab === 'PENDING' }"
        @click="activeTab = 'PENDING'"
      >
        待受理 ({{ pendingRows.length }})
      </button>
      <button
        type="button"
        class="tab"
        :class="{ 'is-active': activeTab === 'HANDLED' }"
        @click="activeTab = 'HANDLED'"
      >
        已受理 ({{ handledRows.length }})
      </button>
    </div>

    <!-- ③ 筛选区 -->
    <div class="card filter-bar">
      <div class="filter-item">
        <input
          v-model="filters.keyword"
          class="input"
          style="width: 200px"
          type="text"
          placeholder="申请编号 / 学号 / 姓名"
        />
      </div>
      <div class="filter-item">
        <select v-model="filters.courseId" class="select" style="width: 180px">
          <option value="">全部课程</option>
          <option v-for="item in data.courseList" :key="item.courseId" :value="item.courseId">
            {{ item.courseName }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filters.timeRange" class="select" style="width: 150px">
          <option value="">全部时间</option>
          <option value="1">今天</option>
          <option value="7">近 7 天</option>
          <option value="30">近 30 天</option>
        </select>
      </div>
      <button type="button" class="btn" @click="onReset">重置</button>
      <div class="spacer"></div>
      <span class="hint">学生提交查分申请后会实时出现在「待受理」</span>
    </div>

    <!-- ④ 列表 -->
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
            <StatusTag v-else-if="column.key === 'result'" :value="row.status === 'REJECTED' ? 'REJECTED' : row.result" />
            <b v-else-if="column.key === 'originalScore'" :style="{ color: scoreColor(row.originalScore) }">
              {{ row.originalScore }}
            </b>
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

    <!-- 受理 / 驳回 / 详情弹窗 -->
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
          <span class="modal-label">课程</span>
          <span>{{ current.courseName }}（{{ current.courseId }}）　{{ current.term }}</span>
        </div>
        <div class="modal-row">
          <span class="modal-label">原成绩</span>
          <span>
            平时 {{ data.scoreOf(current.studentId, current.courseId, current.term)?.usualScore ?? '—' }}
            ／ 期末 {{ data.scoreOf(current.studentId, current.courseId, current.term)?.finalScore ?? '—' }}
            ／ 总评 <b class="text-danger">{{ current.originalScore }}</b>
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

        <!-- 受理表单 -->
        <template v-if="modalMode === 'handle'">
          <div class="field mt-16">
            <span class="label">处理结果 <span class="req">*</span></span>
            <div class="radio-group">
              <label class="radio">
                <input v-model="form.result" type="radio" value="KEEP" />
                <span>维持原成绩</span>
              </label>
              <label class="radio">
                <input v-model="form.result" type="radio" value="CORRECT" />
                <span>更正成绩</span>
              </label>
            </div>
            <span v-if="errors.result" class="err">{{ errors.result }}</span>
          </div>

          <div v-if="form.result === 'CORRECT'" class="field">
            <label class="label" for="corrected">更正后成绩 <span class="req">*</span></label>
            <input
              id="corrected"
              v-model="form.correctedScore"
              class="input"
              :class="{ 'is-error': errors.correctedScore }"
              style="width: 140px"
              type="text"
              inputmode="numeric"
              placeholder="0~100"
            />
            <span v-if="errors.correctedScore" class="err">{{ errors.correctedScore }}</span>
            <span v-else class="hint">更正后会同步更新该生总评成绩与绩点，并写入修改记录</span>
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
              placeholder="例如：经复核，平时分漏计一次作业，已更正为 56 分"
            ></textarea>
            <span v-if="errors.opinion" class="err">{{ errors.opinion }}</span>
          </div>
        </template>

        <!-- 驳回表单 -->
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
              placeholder="例如：已调阅答卷逐题复核，计分无误，驳回申请"
            ></textarea>
            <span v-if="errors.opinion" class="err">{{ errors.opinion }}</span>
          </div>
        </template>

        <!-- 详情 -->
        <template v-else>
          <div class="modal-row">
            <span class="modal-label">处理状态</span>
            <StatusTag :value="current.status" />
          </div>
          <div v-if="current.result" class="modal-row">
            <span class="modal-label">处理结果</span>
            <StatusTag :value="current.result" />
          </div>
          <div v-if="current.correctedScore !== null" class="modal-row">
            <span class="modal-label">更正后成绩</span>
            <b class="text-success">{{ current.correctedScore }}</b>
          </div>
          <div class="modal-row">
            <span class="modal-label">处理意见</span>
            <span>{{ current.opinion || '—' }}</span>
          </div>
          <div class="modal-row">
            <span class="modal-label">受理人</span>
            <span>{{ operatorName(current.handlerId) }}</span>
          </div>
          <div class="modal-row">
            <span class="modal-label">处理时间</span>
            <span>{{ current.handleTime || '—' }}</span>
          </div>
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
