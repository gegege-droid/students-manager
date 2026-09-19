<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { validateAppealReason } from '@/utils/validate'

/** P3 申请查分 —— 对应 FR-P3-01 ~ FR-P3-09 */

const route = useRoute()
const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const QUICK_REASONS = [
  '平时分疑似登分错误，请老师复核。',
  '总评成绩与预期差距较大，请老师复核。',
  '平时分一项显示为空，疑似漏录，请老师复核。'
]

const form = reactive({ courseId: '', reason: '' })
const errors = reactive({ courseId: '', reason: '' })
const submitting = ref(false)
const successVisible = ref(false)
const replyVisible = ref(false)
const lastAppeal = ref(null)
const replyItem = ref(null)

/** 该生已出成绩的课程（FR-P3-01） */
const myScores = computed(() => data.scoreRowsOfStudent(user.loginId))
const selectedScore = computed(
  () => myScores.value.find((row) => row.courseId === form.courseId) || null
)
const myAppeals = computed(() => data.appealRowsOfStudent(user.loginId))
const wordCount = computed(() => form.reason.length)

const columns = [
  { key: 'appealId', title: '申请编号', width: '150px' },
  { key: 'courseName', title: '课程名称', width: '160px' },
  { key: 'originalScore', title: '原成绩', width: '90px', align: 'center' },
  { key: 'reason', title: '申请理由', width: '240px' },
  { key: 'applyTime', title: '申请时间', width: '160px', nowrap: true },
  { key: 'status', title: '状态', width: '100px', align: 'center' },
  { key: 'action', title: '操作 / 结果', width: '120px', align: 'center' }
]

onMounted(() => {
  // 从 P2 带过来的课程号（FR-P2-09 / FR-P3-01）
  const courseId = route.query.courseId
  if (courseId && myScores.value.some((row) => row.courseId === courseId)) {
    form.courseId = String(courseId)
  }
})

function clearError(key) {
  errors[key] = ''
}

function onSubmit() {
  if (submitting.value) return

  // 校验：课程必选、理由 10~200 字、同课程不可重复提交
  errors.courseId = form.courseId ? '' : '请选择要查分的课程'
  errors.reason = validateAppealReason(form.reason)

  const duplicate =
    form.courseId && data.hasPendingAppeal(user.loginId, form.courseId, selectedScore.value?.term)
  if (duplicate) {
    errors.courseId = '该课程已有待受理的查分申请，请勿重复提交'
  }

  if (errors.courseId || errors.reason) return

  submitting.value = true
  const appeal = data.submitAppeal({
    studentId: user.loginId,
    courseId: form.courseId,
    reason: form.reason
  })
  submitting.value = false

  lastAppeal.value = { ...appeal, courseName: selectedScore.value?.courseName || appeal.courseId }
  successVisible.value = true
  // 提交后清空表单（FR-P3-08 的效果：该课程按钮变为不可重复提交）
  form.reason = ''
  errors.reason = ''
  toast.success('查分申请已提交')
}

function onClear() {
  form.courseId = ''
  form.reason = ''
  errors.courseId = ''
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

    <!-- ② 申请表单 -->
    <div class="card">
      <h3 class="card-title">填写查分申请</h3>

      <div class="form-row">
        <div class="course-col">
          <div class="field">
            <label class="label" for="course">课程 <span class="req">*</span></label>
            <select
              id="course"
              v-model="form.courseId"
              class="select"
              :class="{ 'is-error': errors.courseId }"
              @change="clearError('courseId')"
            >
              <option value="">请选择要查分的课程</option>
              <option v-for="row in myScores" :key="row.courseId" :value="row.courseId">
                {{ row.courseName }}（{{ row.courseId }}）
                {{ data.hasPendingAppeal(user.loginId, row.courseId, row.term) ? '· 查分中' : '' }}
              </option>
            </select>
            <span v-if="errors.courseId" class="err">{{ errors.courseId }}</span>
          </div>

          <div class="field">
            <span class="label">原成绩（只读）</span>
            <input
              class="input"
              type="text"
              readonly
              :value="selectedScore ? `总评 ${selectedScore.totalScore}　（平时 ${selectedScore.usualScore} / 期末 ${selectedScore.finalScore}）` : '请先选择课程'"
            />
          </div>
        </div>

        <div class="reason-col">
          <div class="field">
            <label class="label" for="reason">
              申请理由 <span class="req">*</span>（10~200 字）
            </label>
            <textarea
              id="reason"
              v-model="form.reason"
              class="textarea"
              :class="{ 'is-error': errors.reason }"
              rows="4"
              maxlength="200"
              placeholder="请说明查分原因，例如：平时分已按要求提交全部作业，请老师复核登分情况"
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
              v-for="text in QUICK_REASONS"
              :key="text"
              type="button"
              class="tag quick-tag"
              @click="useQuickReason(text)"
            >
              {{ text }}
            </button>
          </div>

          <div class="btn-group mt-12">
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="onSubmit">
              提交申请
            </button>
            <button type="button" class="btn" @click="onClear">清空</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ③ 我的查分申请 -->
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
        <EmptyState title="还没有查分申请" desc="在「成绩查询」页对可疑成绩点击「申请查分」即可发起" />
      </div>
    </div>

    <!-- 提交成功弹窗（FR-P3-06） -->
    <BaseModal v-model="successVisible" title="提交成功" :width="420" confirm-text="知道了" @confirm="successVisible = false">
      <div class="success-body">
        <div class="success-icon">✓</div>
        <p class="success-title">申请已提交</p>
        <p class="hint">申请编号：{{ lastAppeal?.appealId }}</p>
        <p class="hint">课程：{{ lastAppeal?.courseName }}</p>
        <p class="hint mt-8">班主任将在 3 个工作日内受理，可在下方「我的查分申请」查看进度。</p>
      </div>
    </BaseModal>

    <!-- 查看回复 -->
    <BaseModal v-model="replyVisible" title="查分申请处理结果" :width="520" confirm-text="关闭" @confirm="replyVisible = false">
      <div v-if="replyItem" class="reply-body">
        <div class="reply-row">
          <span class="reply-label">申请编号</span>
          <span>{{ replyItem.appealId }}</span>
        </div>
        <div class="reply-row">
          <span class="reply-label">课程</span>
          <span>{{ replyItem.courseName }}（{{ replyItem.courseId }}）</span>
        </div>
        <div class="reply-row">
          <span class="reply-label">原成绩</span>
          <span>{{ replyItem.originalScore }}</span>
        </div>
        <div class="reply-row">
          <span class="reply-label">处理状态</span>
          <StatusTag :value="replyItem.status" />
        </div>
        <div v-if="replyItem.result" class="reply-row">
          <span class="reply-label">处理结果</span>
          <StatusTag :value="replyItem.result" />
        </div>
        <div v-if="replyItem.correctedScore !== null" class="reply-row">
          <span class="reply-label">更正后成绩</span>
          <span class="corrected">{{ replyItem.correctedScore }}</span>
        </div>
        <div class="reply-row">
          <span class="reply-label">处理意见</span>
          <span>{{ replyItem.opinion || '—' }}</span>
        </div>
        <div class="reply-row">
          <span class="reply-label">处理时间</span>
          <span>{{ replyItem.handleTime || '—' }}</span>
        </div>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
.course-col {
  flex: 1 1 260px;
  max-width: 340px;
}

.reason-col {
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
  font-weight: 700;
}
</style>
