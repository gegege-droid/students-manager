<script setup>
import { computed, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import DataTable from '@/components/DataTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { calcTotal, scoreColor } from '@/utils/score'
import { isScoreValue, validateChangeReason } from '@/utils/validate'

/** P5 修改学生成绩 —— 对应 FR-P5-01 ~ FR-P5-08 */

const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const query = reactive({ keyword: '', classId: '', courseId: '' })
const queryError = ref('')
const searched = ref(false)
const matches = ref([])
const target = ref(null)
const newUsual = ref('')
const newFinal = ref('')
const reason = ref('')
const errors = reactive({ score: '', reason: '' })
const confirmVisible = ref(false)

const locked = computed(() => target.value?.status === 'APPEALING')

const newTotal = computed(() => {
  if (!isScoreValue(newUsual.value) || !isScoreValue(newFinal.value)) return null
  return calcTotal(Number(newUsual.value), Number(newFinal.value))
})

const logs = computed(() =>
  target.value ? data.logsOf(target.value.studentId, target.value.courseId) : []
)

const matchColumns = [
  { key: 'studentId', title: '学号', width: '110px' },
  { key: 'studentName', title: '姓名', width: '90px' },
  { key: 'courseName', title: '课程名称', width: '160px' },
  { key: 'usualScore', title: '平时', width: '70px', align: 'center' },
  { key: 'finalScore', title: '期末', width: '70px', align: 'center' },
  { key: 'totalScore', title: '总评', width: '80px', align: 'center' },
  { key: 'status', title: '状态', width: '100px', align: 'center' },
  { key: 'action', title: '操作', width: '100px', align: 'center' }
]

const logColumns = [
  { key: 'operateTime', title: '修改时间', width: '150px', nowrap: true },
  { key: 'operator', title: '操作人', width: '110px' },
  { key: 'courseName', title: '课程', width: '150px' },
  { key: 'changeText', title: '改动内容', width: '230px' },
  { key: 'changeReason', title: '修改原因', width: '250px' },
  { key: 'source', title: '来源', width: '110px', align: 'center' }
]

function onSearch() {
  queryError.value = ''
  target.value = null
  matches.value = []

  const keyword = query.keyword.trim()
  if (!keyword && !query.classId && !query.courseId) {
    searched.value = false
    queryError.value = '请至少输入一个查询条件（学号 / 姓名 / 班级 / 课程）'
    return
  }

  matches.value = data.scores
    .map((score) => {
      const student = data.studentMap[score.studentId]
      const course = data.courseMap[score.courseId]
      if (!student || !course) return null
      return {
        ...score,
        studentName: student.name,
        className: student.className,
        classId: student.classId,
        courseName: course.courseName,
        credit: course.credit,
        status: data.scoreStatusOf(score)
      }
    })
    .filter(Boolean)
    .filter((row) => {
      if (query.classId && row.classId !== query.classId) return false
      if (query.courseId && row.courseId !== query.courseId) return false
      if (keyword) {
        return row.studentId.includes(keyword) || row.studentName.includes(keyword)
      }
      return true
    })

  searched.value = true
  // 唯一命中时直接载入，多条命中时由用户点选
  if (matches.value.length === 1) loadTarget(matches.value[0])
}

function loadTarget(row) {
  target.value = row
  newUsual.value = row.usualScore === null || row.usualScore === undefined ? '' : String(row.usualScore)
  newFinal.value = row.finalScore === null || row.finalScore === undefined ? '' : String(row.finalScore)
  reason.value = ''
  errors.score = ''
  errors.reason = ''
}

function onClear() {
  query.keyword = ''
  query.classId = ''
  query.courseId = ''
  queryError.value = ''
  searched.value = false
  matches.value = []
  target.value = null
}

function onCancelEdit() {
  if (!target.value) return
  newUsual.value = String(target.value.usualScore ?? '')
  newFinal.value = String(target.value.finalScore ?? '')
  reason.value = ''
  errors.score = ''
  errors.reason = ''
}

function onSave() {
  if (!target.value || locked.value) return

  errors.score = ''
  errors.reason = ''
  if (!isScoreValue(newUsual.value) || !isScoreValue(newFinal.value)) {
    errors.score = '成绩必须是 0~100 的整数'
  } else if (
    Number(newUsual.value) === Number(target.value.usualScore) &&
    Number(newFinal.value) === Number(target.value.finalScore)
  ) {
    errors.score = '新成绩与原成绩相同，无需修改'
  }
  errors.reason = validateChangeReason(reason.value)
  if (errors.score || errors.reason) return

  // FR-P5-05：破坏性操作二次确认
  confirmVisible.value = true
}

function onConfirmSave() {
  const scoreId = target.value.scoreId
  const result = data.updateScore({
    studentId: target.value.studentId,
    courseId: target.value.courseId,
    term: target.value.term,
    usualScore: Number(newUsual.value),
    finalScore: Number(newFinal.value),
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
  const found = matches.value.find((row) => row.scoreId === scoreId)
  if (found) loadTarget(found)
}

function operatorName(loginId) {
  return data.userNameOf(loginId)
}

function changeText(log) {
  const parts = []
  if (log.oldUsual !== log.newUsual) parts.push(`平时 ${log.oldUsual}→${log.newUsual}`)
  if (log.oldFinal !== log.newFinal) parts.push(`期末 ${log.oldFinal}→${log.newFinal}`)
  if (log.oldTotal !== log.newTotal) parts.push(`总评 ${log.oldTotal}→${log.newTotal}`)
  return parts.length ? parts.join('；') : '—'
}

const confirmText = computed(() => {
  if (!target.value) return ''
  const parts = []
  if (Number(newUsual.value) !== Number(target.value.usualScore)) {
    parts.push(`平时 ${target.value.usualScore}→${newUsual.value}`)
  }
  if (Number(newFinal.value) !== Number(target.value.finalScore)) {
    parts.push(`期末 ${target.value.finalScore}→${newFinal.value}`)
  }
  if (newTotal.value !== null && newTotal.value !== target.value.totalScore) {
    parts.push(`总评 ${target.value.totalScore}→${newTotal.value}`)
  }
  return parts.join('，')
})
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">修改学生成绩</h2>
      <span class="breadcrumb">首页 / 修改成绩</span>
    </div>

    <!-- ② 查询区 -->
    <div class="card filter-bar">
      <div class="filter-item">
        <label class="label" for="keyword">学号/姓名</label>
        <input
          id="keyword"
          v-model="query.keyword"
          class="input"
          style="width: 180px"
          type="text"
          placeholder="如 2021001 或 张三"
          @keyup.enter="onSearch"
        />
      </div>
      <div class="filter-item">
        <label class="label" for="class">班级</label>
        <select id="class" v-model="query.classId" class="select" style="width: 170px">
          <option value="">全部班级</option>
          <option v-for="item in data.classOptions" :key="item.classId" :value="item.classId">
            {{ item.className }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label class="label" for="course">课程</label>
        <select id="course" v-model="query.courseId" class="select" style="width: 190px">
          <option value="">全部课程</option>
          <option v-for="item in data.courseList" :key="item.courseId" :value="item.courseId">
            {{ item.courseName }}
          </option>
        </select>
      </div>
      <button type="button" class="btn btn-primary" @click="onSearch">查询</button>
      <button type="button" class="btn" @click="onClear">清空</button>
    </div>

    <div v-if="queryError" class="alert alert-warning mt-16">⚠ {{ queryError }}</div>

    <!-- 多条命中时让用户点选 -->
    <div v-if="matches.length > 1 && !target" class="card card-flush mt-16">
      <div class="list-head">
        <h3 class="card-title" style="margin: 0">匹配到 {{ matches.length }} 条成绩记录</h3>
        <span class="hint">点击「修改」载入要更正的成绩</span>
      </div>
      <DataTable :columns="matchColumns" :rows="matches" row-key="scoreId">
        <template #cell="{ row, column }">
          <StatusTag v-if="column.key === 'status'" :value="row.status" />
          <button v-else-if="column.key === 'action'" type="button" class="btn btn-ghost btn-sm" @click="loadTarget(row)">
            修改
          </button>
          <b v-else-if="column.key === 'totalScore'" :style="{ color: scoreColor(row.totalScore) }">
            {{ row.totalScore }}
          </b>
          <template v-else>{{ row[column.key] ?? '—' }}</template>
        </template>
      </DataTable>
    </div>

    <!-- 查分中锁定提示（FR-P5-07） -->
    <div v-if="target && locked" class="alert alert-warning mt-16">
      ⚠ 该成绩存在待受理的查分申请，请先到「受理查分申请」完成受理后再修改。
    </div>

    <!-- ④ 原成绩 / 新成绩对照 -->
    <div v-if="target" class="card mt-16">
      <div class="row-between mb-12">
        <b>
          {{ target.studentId }}　{{ target.studentName }}　{{ target.className }}　·　{{ target.courseName }}
        </b>
        <StatusTag :value="target.status" />
      </div>

      <table class="table compare-table">
        <thead>
          <tr>
            <th style="width: 140px">项目</th>
            <th class="c" style="width: 200px">原成绩（只读）</th>
            <th class="c" style="width: 200px">新成绩（可编辑）</th>
            <th>说明</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>平时成绩</td>
            <td class="c">
              <span class="origin">{{ target.usualScore ?? '—' }}</span>
            </td>
            <td class="c">
              <input v-model="newUsual" class="input input-sm" type="text" inputmode="numeric" :disabled="locked" />
            </td>
            <td class="hint">
              {{ Number(newUsual) !== Number(target.usualScore) ? `改动：${target.usualScore} → ${newUsual}` : '未改动' }}
            </td>
          </tr>
          <tr>
            <td>期末成绩</td>
            <td class="c">
              <span class="origin">{{ target.finalScore ?? '—' }}</span>
            </td>
            <td class="c">
              <input v-model="newFinal" class="input input-sm" type="text" inputmode="numeric" :disabled="locked" />
            </td>
            <td class="hint">
              {{ Number(newFinal) !== Number(target.finalScore) ? `改动：${target.finalScore} → ${newFinal}` : '未改动' }}
            </td>
          </tr>
          <tr>
            <td>总评成绩</td>
            <td class="c">
              <b :style="{ color: scoreColor(target.totalScore) }">{{ target.totalScore }}</b>
            </td>
            <td class="c">
              <span v-if="newTotal === null" class="text-secondary">—</span>
              <b v-else :style="{ color: scoreColor(newTotal) }">{{ newTotal }}</b>
            </td>
            <td class="hint">自动计算，随新成绩实时更新</td>
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
        <span class="hint">保存前会弹出二次确认，原成绩将保留在修改记录中</span>
      </div>
    </div>

    <!-- ⑤ 修改记录 -->
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
          :desc="`没有找到符合条件的成绩，请检查学号 / 姓名是否正确，或更换班级与课程`"
        />
        <EmptyState
          v-else
          title="请输入条件查询学生成绩"
          desc="支持按学号、姓名、班级、课程组合查询；查询到记录后可修改平时与期末成绩"
        />
      </div>
    </div>

    <!-- 二次确认 -->
    <BaseModal
      v-model="confirmVisible"
      title="确认修改成绩？"
      :width="460"
      confirm-text="确认修改"
      @confirm="onConfirmSave"
    >
      <p>本次改动：{{ confirmText || '—' }}</p>
      <p class="hint mt-8">修改后原成绩将保留在修改记录中，操作人与操作时间会被记录，无法删除。</p>
    </BaseModal>
  </div>
</template>

<style scoped>
.compare-table td {
  vertical-align: middle;
}

.origin {
  display: inline-block;
  min-width: 70px;
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
