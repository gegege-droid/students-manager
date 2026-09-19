<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { gradeLevel } from '@/utils/score'

/** P5 录入月考成绩 —— 按「班级 + 月考场次 + 科目」批量录入 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const form = reactive({ classId: '', examId: '', subjectId: '' })
const draft = ref([])
const successVisible = ref(false)
const savedCount = ref(0)

const ready = computed(() => Boolean(form.classId && form.examId && form.subjectId))
const subject = computed(() => data.subjectMap[form.subjectId] || null)
const fullScore = computed(() => subject.value?.fullScore || 100)

const columns = [
  { key: 'index', title: '序号', width: '70px', align: 'center' },
  { key: 'studentId', title: '学号', width: '140px' },
  { key: 'name', title: '姓名', width: '130px' },
  { key: 'combination', title: '选科组合', width: '150px' },
  { key: 'score', title: '分数', width: '220px', align: 'center' },
  { key: 'level', title: '等级', width: '160px', align: 'center' },
  { key: 'status', title: '状态', width: '180px', align: 'center' }
]

const recordedCount = computed(() => draft.value.filter((row) => row.recorded || valid(row)).length)

function valid(row) {
  const value = row.input
  if (value === '' || value === null || value === undefined) return false
  const text = String(value).trim()
  if (!/^\d{1,3}$/.test(text)) return false
  const num = Number(text)
  return num >= 0 && num <= fullScore.value
}

function validateRow(row) {
  const value = row.input
  if (value === '' || value === null || value === undefined) {
    row.error = ''
    return
  }
  row.error = valid(row) ? '' : `分数必须是 0~${fullScore.value} 的整数`
}

function loadRoster() {
  if (!ready.value) {
    draft.value = []
    return
  }
  draft.value = data.rosterOf(form.classId, form.examId, form.subjectId).map((row) => ({
    ...row,
    input: row.recorded ? String(row.score) : '',
    error: ''
  }))
}

onMounted(() => {
  form.classId = user.manageClassIds?.[0] || data.gradeInfo.classId
  form.examId = data.sessionList[data.sessionList.length - 1]?.examId || ''
  form.subjectId = data.subjectList[0]?.subjectId || ''
  loadRoster()
})

watch(() => [form.classId, form.examId, form.subjectId], loadRoster)

function onReset() {
  draft.value.forEach((row) => {
    if (row.recorded) return
    row.input = ''
    row.error = ''
  })
  toast.info('已清空本次录入的内容')
}

function onSave() {
  const invalid = draft.value.find((row) => !row.recorded && row.error)
  if (invalid) {
    toast.error(`${invalid.name} 的分数不合法，请修正后再保存`)
    return
  }

  const rows = draft.value.filter((row) => !row.recorded && valid(row))
  if (!rows.length) {
    toast.error('请至少录入一条成绩')
    return
  }

  savedCount.value = data.saveBatch({
    examId: form.examId,
    subjectId: form.subjectId,
    rows: rows.map((row) => ({ studentId: row.studentId, score: Number(row.input) })),
    recorderId: user.loginId
  })
  successVisible.value = true
  loadRoster()
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">录入月考成绩</h2>
      <span class="breadcrumb">首页 / 录入月考成绩</span>
    </div>

    <div class="card filter-bar">
      <div class="filter-item">
        <label class="label" for="class">班级 <span class="req">*</span></label>
        <select id="class" v-model="form.classId" class="select" style="width: 170px">
          <option value="">请选择班级</option>
          <option :value="data.gradeInfo.classId">{{ data.gradeInfo.className }}</option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="exam">月考场次 <span class="req">*</span></label>
        <select id="exam" v-model="form.examId" class="select" style="width: 170px">
          <option value="">请选择场次</option>
          <option v-for="item in data.sessionList" :key="item.examId" :value="item.examId">
            {{ item.name }}{{ item.pending ? '（待录入）' : '' }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="subject">科目 <span class="req">*</span></label>
        <select id="subject" v-model="form.subjectId" class="select" style="width: 170px">
          <option value="">请选择科目</option>
          <option v-for="item in data.subjectList" :key="item.subjectId" :value="item.subjectId">
            {{ item.name }}（{{ item.fullScore }} 分）
          </option>
        </select>
      </div>
      <div class="spacer"></div>
      <span class="hint">满分 {{ fullScore }} 分 · 已录入 {{ recordedCount }} / 共 {{ draft.length }} 人</span>
    </div>

    <div class="toolbar mt-16">
      <span class="hint">共 {{ draft.length }} 名学生（只列出选考该科的学生）</span>
      <div class="spacer"></div>
      <button type="button" class="btn" :disabled="!ready" @click="onReset">重置</button>
      <button type="button" class="btn btn-primary" :disabled="!ready" @click="onSave">保存全部成绩</button>
    </div>

    <div class="card card-flush">
      <template v-if="ready && draft.length">
        <DataTable :columns="columns" :rows="draft" row-key="studentId">
          <template #cell="{ row, column, index }">
            <template v-if="column.key === 'index'">{{ index + 1 }}</template>
            <template v-else-if="column.key === 'score'">
              <input
                v-model="row.input"
                class="input input-sm score-input"
                :class="{ 'is-error': row.error }"
                type="text"
                inputmode="numeric"
                :disabled="row.recorded"
                :placeholder="`0~${fullScore}`"
                @input="validateRow(row)"
              />
            </template>
            <template v-else-if="column.key === 'level'">
              <span
                v-if="!valid(row)"
                class="text-secondary"
              >—</span>
              <span
                v-else
                class="level"
                :style="{
                  color: gradeLevel(Number(row.input) / fullScore).color,
                  background: gradeLevel(Number(row.input) / fullScore).bg,
                  borderColor: gradeLevel(Number(row.input) / fullScore).border
                }"
              >
                {{ gradeLevel(Number(row.input) / fullScore).label }}
              </span>
            </template>
            <template v-else-if="column.key === 'status'">
              <span v-if="row.error" class="err" style="margin: 0">{{ row.error }}</span>
              <StatusTag v-else-if="row.recorded" value="ACCEPTED" label="已录入" />
              <StatusTag v-else-if="valid(row)" value="MODIFIED" label="待保存" />
              <StatusTag v-else value="NORMAL" label="未录入" />
            </template>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
      </template>

      <div v-else class="empty-wrap">
        <EmptyState
          title="请先选择班级、月考场次与科目"
          desc="选择后会加载选考该科的学生名单；每次月考每科满分不同，录入时会自动校验 0~满分"
        />
      </div>
    </div>

    <BaseModal v-model="successVisible" title="保存成功" :width="420" confirm-text="知道了" @confirm="successVisible = false">
      <div class="success-body">
        <div class="success-icon">✓</div>
        <p class="success-title">已成功录入 {{ savedCount }} 条成绩</p>
        <p class="hint mt-8">全科录完后，学生端的「高考成绩预测」会自动把新成绩纳入趋势计算。</p>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
.score-input {
  width: 110px;
  text-align: center;
}

.level {
  display: inline-block;
  font-size: 12px;
  line-height: 20px;
  padding: 0 9px;
  border-radius: 10px;
  border: 1px solid;
}

.empty-wrap {
  padding: 16px;
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
}
</style>
