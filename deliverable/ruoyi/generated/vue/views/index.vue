<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学号" prop="studentId">
        <el-input
          v-model="queryParams.studentId"
          placeholder="请输入学号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="月考场次" prop="examId">
        <el-input
          v-model="queryParams.examId"
          placeholder="请输入月考场次编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="科目" prop="subjectId">
        <el-input
          v-model="queryParams.subjectId"
          placeholder="请输入科目编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="得分" prop="score">
        <el-input
          v-model="queryParams.score"
          placeholder="请输入得分"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="得分率" prop="scoreRate">
        <el-input
          v-model="queryParams.scoreRate"
          placeholder="请输入得分率 = score / full_score"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="等级" prop="level">
        <el-input
          v-model="queryParams.level"
          placeholder="请输入等级"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="录入人" prop="recorderId">
        <el-input
          v-model="queryParams.recorderId"
          placeholder="请输入录入人"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="录入时间" prop="recordTime">
        <el-date-picker clearable
          v-model="queryParams.recordTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择录入时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:score:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:score:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:score:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:score:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="scoreList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="成绩ID" align="center" prop="scoreId" width="220" show-overflow-tooltip />
      <el-table-column label="学号" align="center" prop="studentId" width="110" />
      <el-table-column label="月考场次" align="center" prop="examId" width="100" />
      <el-table-column label="科目" align="center" prop="subjectId" width="120" />
      <el-table-column label="得分" align="center" prop="score" width="80" />
      <el-table-column label="得分率" align="center" prop="scoreRate" width="100" />
      <el-table-column label="等级" align="center" prop="level" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="110" />
      <el-table-column label="录入人" align="center" prop="recorderId" width="100" />
      <el-table-column label="录入时间" align="center" prop="recordTime" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.recordTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:score:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:score:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改月考成绩对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="scoreRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学号" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="月考场次" prop="examId">
              <el-input v-model="form.examId" placeholder="请输入月考场次编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="科目" prop="subjectId">
              <el-input v-model="form.subjectId" placeholder="请输入科目编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="得分" prop="score">
              <el-input v-model="form.score" placeholder="请输入得分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="得分率" prop="scoreRate">
              <el-input v-model="form.scoreRate" placeholder="请输入得分率 = score / full_score" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="等级" prop="level">
              <el-input v-model="form.level" placeholder="请输入等级" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="录入人" prop="recorderId">
              <el-input v-model="form.recorderId" placeholder="请输入录入人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="录入时间" prop="recordTime">
              <el-date-picker clearable
                v-model="form.recordTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择录入时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="删除标志" prop="delFlag">
              <el-input v-model="form.delFlag" placeholder="请输入删除标志" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Score">
import { listScore, getScore, delScore, addScore, updateScore } from "@/api/business/score"

const { proxy } = getCurrentInstance()

const scoreList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: undefined,
    examId: undefined,
    subjectId: undefined,
    score: undefined,
    scoreRate: undefined,
    level: undefined,
    status: undefined,
    recorderId: undefined,
    recordTime: undefined,
  },
  rules: {
    studentId: [
      { required: true, message: "学号不能为空", trigger: "blur" }
    ],
    examId: [
      { required: true, message: "月考场次编号不能为空", trigger: "blur" }
    ],
    subjectId: [
      { required: true, message: "科目编号不能为空", trigger: "blur" }
    ],
    score: [
      { required: true, message: "得分不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询月考成绩列表 */
function getList() {
  loading.value = true
  listScore(queryParams.value).then(response => {
    scoreList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    scoreId: null,
    studentId: null,
    examId: null,
    subjectId: null,
    score: null,
    scoreRate: null,
    level: null,
    status: null,
    recorderId: null,
    recordTime: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("scoreRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.scoreId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加月考成绩"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _scoreId = row.scoreId || ids.value
  getScore(_scoreId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改月考成绩"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["scoreRef"].validate(valid => {
    if (valid) {
      if (form.value.scoreId != null) {
        updateScore(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addScore(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _scoreIds = row.scoreId || ids.value
  proxy.$modal.confirm('是否确认删除月考成绩编号为"' + _scoreIds + '"的数据项？').then(function() {
    return delScore(_scoreIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/score/export', {
    ...queryParams.value
  }, `score_${new Date().getTime()}.xlsx`)
}

getList()
</script>
