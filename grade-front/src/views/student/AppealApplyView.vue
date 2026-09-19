<script setup>
import { computed, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { validateAppealReason } from '@/utils/validate'

/** P4 申请查分 —— 对某次月考的某个科目发起复核申请 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const QUICK_REASONS = [
  '该题我写了完整过程，估分与公布分数差距较大，申请复核计分。',
  '选择题答案与我作答一致，怀疑机读卡识别有误，申请核查。',
  '主观题得分明显低于平时水平，怀疑漏加要点分，申请复核。'
]

const form = reactive({ key: '', reason: '' })
const errors = reactive({ key: '', reason: '' })
const submitting = ref(false)
const successVisible = ref(false)
const replyVisible = ref(false)
const lastAppeal = ref(null)
const replyItem = ref(null)

const items = computed(() => data.appealableItems(user.loginId))
const selected = computed(() => items.value.find((item) => item.key === form.key) || null)
const myAppeals = computed(() => data.appealRowsOfStudent(user.loginId))
const wordCount = computed(() => form.reason.length)

const columns = [
  { key: 'appealId', title: '申请编号', width: '150px' },
  { key: 'examName', title: '月考场次', width: '130px' },
  { key: 'subjectName', title: '科目', width: '110px' },
  { key: 'originalScore', title: '原分数', width: '90px', align: 'center' },
  { key: 'reason', title: '申请理由', width: '280px' },
  { key: 'applyTime', title: '申请时间', width: '150px', nowrap: true },
  { key: 'status', title: '状态', width: '100px', align: 'center' },
  { key: 'action', title: '操作 / 结果', width: '120px', align: 'center' }
]

function clearError(key) {
  errors[key] = ''
}

function onSubmit() {
  if (submitting.value) return

  errors.key = form.key ? '' : '请选择要申请查分的月考场次与科目'
  errors.reason = validateAppealReason(form.reason)
  if (form.key && selected.value?.pending) {
    errors.key = '该场次该科目已有待受理的查分申请，请勿重复提交'
  }
  if (errors.key || errors.reason) return

  submitting.value = true
  const appeal = data.submitAppeal({
    studentId: user.loginId,
    examId: selected.value.examId,
    subjectId: selected.value.subjectId,
    reason: form.reason
  })
  submitting.value = false

  lastAppeal.value = appeal
  successVisible.value = true
  form.key = ''
  form.reason = ''
  toast.success('查分申请已提交')
}

function onClear() {
  form.key = ''
  form.reason = ''
  errors.key = ''
  errors.reason = ''
}

function useQuickReason(text) {
  form.reason = text
  errors.reason = ''
}

function openReply(row) {
  replyItem.value = row
  replyVisible.value = true
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">申请查分</h2>
      <span class="breadcrumb">首页 / 申请查分</span>
    </div>

    <div class="card">
      <h3 class="card-title">填写查分申请</h3>
      <div class="form-row">
        <div class="left-col">
          <div class="field">
            <label class="label" for="item">月考场次 · 科目 <span class="req">*</span></label>
            <select
              id="item"
              v-model="form.key"
              class="select"
              :class="{ 'is-error': errors.key }"
              @change="clearError('key')"
            >
              <option value="">请选择要查分的场次与科目</option>
              <option v-for="item in items" :key="item.key" :value="item.key">
                {{ item.examName }} · {{ item.subjectName }}{{ item.pending ? '（查分中）' : '' }}
              </option>
            </select>
            <span v-if="errors.key" class="err">{{ errors.key }}</span>
          </div>
          <div class="field">
            <span class="label">原分数（只读）</span>
            <input
              class="input"
              type="text"
              readonly
              :value="selected ? `${selected.score} / ${selected.fullScore} 分` : '请先选择场次与科目'"
            />
          </div>
        </div>

        <div class="right-col">
          <div class="field">
            <label class="label" for="reason">申请理由 <span class="req">*</span>（10~200 字）</label>
            <textarea
              id="reason"
              v-model="form.reason"
              class="textarea"
              :class="{ 'is-error': errors.reason }"
              rows="4"
              maxlength="200"
              placeholder="请说明查分原因，例如：该题我写了完整过程，估分与公布分数差距较大，申请复核计分"
              @input="clearError('reason')"
            ></textarea>
            <div class="row-between">
              <span v-if="errors.reason" class="err">{{ errors.reason }}</span>
              <span v-else class="hint">请客观描述问题，便于老师复核</span>
              <span class="hint">{{ wordCount }}/200</span>
            </div>
          </div>

          <div class="quick-reasons">
            <span class="hint">常用理由：</span>
            <button
              v-for="(text, index) in QUICK_REASONS"
              :key="text"
              type="button"
              class="tag quick-tag"
              @click="useQuickReason(text)"
            >
              理由 {{ index + 1 }}
            </button>
          </div>

          <div class="btn-group mt-12">
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="onSubmit">提交申请</button>
            <button type="button" class="btn" @click="onClear">清空</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card card-flush section-gap">
      <div class="list-head">
        <h3 class="card-title" style="margin: 0">我的查分申请</h3>
        <span class="hint">共 {{ myAppeals.length }} 条</span>
      </div>

      <template v-if="myAppeals.length">
        <DataTable :columns="columns" :rows="myAppeals" row-key="appealId">
          <template #cell="{ row, column }">
            <StatusTag v-if="column.key === 'status'" :value="row.status" />
            <template v-else-if="column.key === 'action'">
              <button v-if="row.status !== 'PENDING'" type="button" class="btn btn-ghost btn-sm" @click="openReply(row)">
                查看回复
              </button>
              <span v-else class="text-secondary">—</span>
            </template>
            <span v-else-if="column.key === 'reason'" class="ellipsis" :title="row.reason">{{ row.reason }}</span>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
      </template>

      <div v-else class="empty-wrap">
        <EmptyState title="还没有查分申请" desc="在「月考成绩查询」里发现某科分数异常时，可在这里发起复核申请" />
      </div>
    </div>

    <BaseModal v-model="successVisible" title="提交成功" :width="420" confirm-text="知道了" @confirm="successVisible = false">
      <div class="success-body">
        <div class="success-icon">✓</div>
        <p class="success-title">申请已提交</p>
        <p class="hint">申请编号：{{ lastAppeal?.appealId }}</p>
        <p class="hint">场次科目：{{ lastAppeal?.examName }} · {{ lastAppeal?.subjectName }}</p>
        <p class="hint mt-8">班主任将在 3 个工作日内受理，可在下方列表查看进度。</p>
      </div>
    </BaseModal>

    <BaseModal v-model="replyVisible" title="查分申请处理结果" :width="520" confirm-text="关闭" @confirm="replyVisible = false">
      <div v-if="replyItem">
        <div class="reply-row"><span class="reply-label">申请编号</span><span>{{ replyItem.appealId }}</span></div>
        <div class="reply-row"><span class="reply-label">场次科目</span><span>{{ replyItem.examName }} · {{ replyItem.subjectName }}</span></div>
        <div class="reply-row"><span class="reply-label">原分数</span><span>{{ replyItem.originalScore }} / {{ replyItem.fullScore }}</span></div>
        <div class="reply-row"><span class="reply-label">处理状态</span><StatusTag :value="replyItem.status" /></div>
        <div v-if="replyItem.result" class="reply-row">
          <span class="reply-label">处理结果</span><StatusTag :value="replyItem.result" />
        </div>
        <div v-if="replyItem.correctedScore !== null" class="reply-row">
          <span class="reply-label">更正后分数</span><b class="corrected">{{ replyItem.correctedScore }}</b>
        </div>
        <div class="reply-row"><span class="reply-label">处理意见</span><span>{{ replyItem.opinion || '—' }}</span></div>
        <div class="reply-row"><span class="reply-label">处理时间</span><span>{{ replyItem.handleTime || '—' }}</span></div>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
.left-col {
  flex: 1 1 260px;
  max-width: 340px;
}

.right-col {
  flex: 2 1 420px;
}

.quick-reasons {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.quick-tag {
  background: #f2f5f9;
  border-color: #d3dced;
  color: #3f5c7d;
  cursor: pointer;
  font-family: inherit;
  line-height: 24px;
}

.quick-tag:hover {
  background: #e6eef8;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
}

.empty-wrap {
  padding: 0 16px 16px;
}

.success-body {
  text-align: center;
}

.success-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: #eaf4ee;
  color: var(--color-success);
  font-size: 24px;
  line-height: 48px;
}

.success-title {
  font-size: var(--font-size-section);
  font-weight: 700;
  margin-bottom: 6px;
}

.reply-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed #edf0f4;
  font-size: 13px;
}

.reply-row:last-child {
  border-bottom: none;
}

.reply-label {
  flex: 0 0 88px;
  color: var(--color-text-secondary);
}

.corrected {
  color: var(--color-success);
}
</style>
