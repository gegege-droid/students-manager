<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { calcTotal, scoreColor } from '@/utils/score'
import { isScoreValue } from '@/utils/validate'

/** P4 添加学生成绩 —— 对应 FR-P4-01 ~ FR-P4-09 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const form = reactive({ classId: '', courseId: '', term: '2024-2025-1' })
const draft = ref([])
const successVisible = ref(false)
const savedCount = ref(0)

const ready = computed(() => Boolean(form.classId && form.courseId))

const columns = [
  { key: 'index', title: '序号', width: '64px', align: 'center' },
  { key: 'studentId', title: '学号', width: '120px' },
  { key: 'name', title: '姓名', width: '110px' },
  { key: 'usualScore', title: '平时成绩', width: '160px', align: 'center' },
  { key: 'finalScore', title: '期末成绩', width: '160px', align: 'center' },
  { key: 'totalScore', title: '总评成绩', width: '140px', align: 'center' },
  { key: 'status', title: '状态', width: '140px', align: 'center' }
]

/** 已录入人数（含本次未保存的填写） */
const recordedCount = computed(
  () => draft.value.filter((row) => row.recorded || isComplete(row)).length
)

function isComplete(row) {
  return isScoreValue(row.usualInput) && isScoreValue(row.finalInput)
}

function totalOf(row) {
  if (!isComplete(row)) return null
  return calcTotal(Number(row.usualInput), Number(row.finalInput))
}

function validateRow(row) {
  const usual = row.usualInput
  const final = row.finalInput
  if (usual === '' && final === '') {
    row.error = ''
    return
  }
  if (usual !== '' && !isScoreValue(usual)) {
    row.error = '平时成绩必须是 0~100 的整数'
    return
  }
  if (final !== '' && !isScoreValue(final)) {
    row.error = '期末成绩必须是 0~100 的整数'
    return
  }
  if (usual === '' || final === '') {
    row.error = '请同时填写平时成绩与期末成绩'
    return
  }
  row.error = ''
}

function loadRoster() {
  if (!ready.value) {
    draft.value = []
    return
  }
  draft.value = data.rosterOf(form.classId, form.courseId, form.term).map((row) => ({
    ...row,
    usualInput: row.recorded ? String(row.usualScore ?? '') : '',
    finalInput: row.recorded ? String(row.finalScore ?? '') : '',
    error: ''
  }))
}

onMounted(() => {
  // 默认带入班主任所带班级，减少演示时的手工操作
  form.classId = user.manageClassIds?.[0] || data.classOptions[0]?.classId || ''
  form.courseId = data.courseList[0]?.courseId || ''
  loadRoster()
})

watch(() => [form.classId, form.courseId, form.term], loadRoster)

function onReset() {
  draft.value.forEach((row) => {
    if (row.recorded) return
    row.usualInput = ''
    row.finalInput = ''
    row.error = ''
  })
  toast.info('已清空本次录入的内容')
}

function onSave() {
  const invalid = draft.value.find((row) => !row.recorded && row.error)
  if (invalid) {
    toast.error(`${invalid.name} 的成绩不合法，请修正后再保存`)
    return
  }

  const rows = draft.value.filter((row) => !row.recorded && isComplete(row))
  if (!rows.length) {
    toast.error('请至少录入一条完整的成绩（平时 + 期末）')
    return
  }

  savedCount.value = data.saveBatch({
    courseId: form.courseId,
    term: form.term,
    rows: rows.map((row) => ({
      studentId: row.studentId,
      usualScore: Number(row.usualInput),
      finalScore: Number(row.finalInput)
    })),
    recorderId: user.loginId
  })

  successVisible.value = true
  loadRoster()
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">添加学生成绩</h2>
      <span class="breadcrumb">首页 / 添加成绩</span>
    </div>

    <!-- ② 选择班级与课程 -->
    <div class="card filter-bar">
      <div class="filter-item">
        <label class="label" for="class">班级 <span class="req">*</span></label>
        <select id="class" v-model="form.classId" class="select" style="width: 180px">
          <option value="">请选择班级</option>
          <option v-for="item in data.classOptions" :key="item.classId" :value="item.classId">
            {{ item.className }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="course">课程 <span class="req">*</span></label>
        <select id="course" v-model="form.courseId" class="select" style="width: 200px">
          <option value="">请选择课程</option>
          <option v-for="item in data.courseList" :key="item.courseId" :value="item.courseId">
            {{ item.courseName }}（{{ item.courseId }}）
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="term">学期</label>
        <select id="term" v-model="form.term" class="select" style="width: 150px">
          <option v-for="item in data.termOptions" :key="item" :value="item">{{ item }}</option>
        </select>
      </div>
      <div class="spacer"></div>
      <span class="hint">已录入 {{ recordedCount }} / 共 {{ draft.length }} 人</span>
    </div>

    <!-- ③ 工具栏 -->
    <div class="toolbar mt-16">
      <span class="hint">共 {{ draft.length }} 名学生</span>
      <span class="hint">·</span>
      <span class="hint">总评 = 平时 × 40% + 期末 × 60%，失焦后自动计算</span>
      <div class="spacer"></div>
      <button type="button" class="btn" :disabled="!ready" @click="onReset">重置</button>
      <button type="button" class="btn btn-primary" :disabled="!ready" @click="onSave">保存全部成绩</button>
    </div>

    <!-- ④ 录入表格 -->
    <div class="card card-flush">
      <template v-if="ready && draft.length">
        <DataTable :columns="columns" :rows="draft" row-key="studentId">
          <template #cell="{ row, column, index }">
            <template v-if="column.key === 'index'">{{ index + 1 }}</template>
            <template v-else-if="column.key === 'usualScore'">
              <input
                v-model="row.usualInput"
                class="input input-sm"
                :class="{ 'is-error': row.error }"
                type="text"
                inputmode="numeric"
                :disabled="row.recorded"
                placeholder="0~100"
                @input="validateRow(row)"
              />
            </template>
            <template v-else-if="column.key === 'finalScore'">
              <input
                v-model="row.finalInput"
                class="input input-sm"
                :class="{ 'is-error': row.error }"
                type="text"
                inputmode="numeric"
                :disabled="row.recorded"
                placeholder="0~100"
                @input="validateRow(row)"
              />
            </template>
            <template v-else-if="column.key === 'totalScore'">
              <span v-if="totalOf(row) === null" class="text-secondary">—</span>
              <b v-else :style="{ color: scoreColor(totalOf(row)) }">{{ totalOf(row) }}</b>
            </template>
            <template v-else-if="column.key === 'status'">
              <span v-if="row.error" class="err" style="margin: 0">{{ row.error }}</span>
              <StatusTag v-else-if="row.recorded" value="ACCEPTED" label="已录入" />
              <StatusTag v-else-if="isComplete(row)" value="MODIFIED" label="待保存" />
              <StatusTag v-else value="NORMAL" label="未录入" />
            </template>
            <template v-else>{{ row[column.key] ?? '—' }}</template>
          </template>
        </DataTable>
      </template>

      <div v-else class="empty-wrap">
        <EmptyState
          title="请先选择班级与课程"
          desc="选择后会自动加载该班学生名单，平时成绩与期末成绩可直接在表格中录入"
        />
      </div>
    </div>

    <BaseModal v-model="successVisible" title="保存成功" :width="420" confirm-text="知道了" @confirm="successVisible = false">
      <div class="success-body">
        <div class="success-icon">✓</div>
        <p class="success-title">已成功录入 {{ savedCount }} 条成绩</p>
        <p class="hint mt-8">总评与绩点已按公式自动计算，可在「修改成绩」页查看与更正。</p>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
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
