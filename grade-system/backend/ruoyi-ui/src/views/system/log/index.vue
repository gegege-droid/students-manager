<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学号" prop="studentId">
        <el-input
          v-model="queryParams.studentId"
          placeholder="请输入学号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="课程编号" prop="courseId">
        <el-input
          v-model="queryParams.courseId"
          placeholder="请输入课程编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学期" prop="term">
        <el-input
          v-model="queryParams.term"
          placeholder="请输入学期"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:log:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:log:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:log:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:log:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志编号" align="center" prop="logId" />
      <el-table-column label="成绩编号" align="center" prop="scoreId" />
      <el-table-column label="学号" align="center" prop="studentId" />
      <el-table-column label="课程编号" align="center" prop="courseId" />
      <el-table-column label="学期" align="center" prop="term" />
      <el-table-column label="原平时成绩" align="center" prop="oldUsual" />
      <el-table-column label="原期末成绩" align="center" prop="oldFinal" />
      <el-table-column label="原总评成绩" align="center" prop="oldTotal" />
      <el-table-column label="新平时成绩" align="center" prop="newUsual" />
      <el-table-column label="新期末成绩" align="center" prop="newFinal" />
      <el-table-column label="新总评成绩" align="center" prop="newTotal" />
      <el-table-column label="修改原因" align="center" prop="changeReason" />
      <el-table-column label="操作人工号" align="center" prop="operatorId" />
      <el-table-column label="操作时间" align="center" prop="operateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.operateTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="source" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:log:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:log:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改成绩修改记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="成绩编号" prop="scoreId">
          <el-input v-model="form.scoreId" placeholder="请输入成绩编号" />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="课程编号" prop="courseId">
          <el-input v-model="form.courseId" placeholder="请输入课程编号" />
        </el-form-item>
        <el-form-item label="学期" prop="term">
          <el-input v-model="form.term" placeholder="请输入学期" />
        </el-form-item>
        <el-form-item label="原平时成绩" prop="oldUsual">
          <el-input v-model="form.oldUsual" placeholder="请输入原平时成绩" />
        </el-form-item>
        <el-form-item label="原期末成绩" prop="oldFinal">
          <el-input v-model="form.oldFinal" placeholder="请输入原期末成绩" />
        </el-form-item>
        <el-form-item label="原总评成绩" prop="oldTotal">
          <el-input v-model="form.oldTotal" placeholder="请输入原总评成绩" />
        </el-form-item>
        <el-form-item label="新平时成绩" prop="newUsual">
          <el-input v-model="form.newUsual" placeholder="请输入新平时成绩" />
        </el-form-item>
        <el-form-item label="新期末成绩" prop="newFinal">
          <el-input v-model="form.newFinal" placeholder="请输入新期末成绩" />
        </el-form-item>
        <el-form-item label="新总评成绩" prop="newTotal">
          <el-input v-model="form.newTotal" placeholder="请输入新总评成绩" />
        </el-form-item>
        <el-form-item label="修改原因" prop="changeReason">
          <el-input v-model="form.changeReason" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="操作人工号" prop="operatorId">
          <el-input v-model="form.operatorId" placeholder="请输入操作人工号" />
        </el-form-item>
        <el-form-item label="操作时间" prop="operateTime">
          <el-date-picker clearable
            v-model="form.operateTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择操作时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="form.source" placeholder="请输入来源" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLog, getLog, delLog, addLog, updateLog } from "@/api/system/log"

export default {
  name: "Log",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 成绩修改记录表格数据
      logList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentId: null,
        courseId: null,
        term: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        logId: [
          { required: true, message: "日志编号不能为空", trigger: "blur" }
        ],
        scoreId: [
          { required: true, message: "成绩编号不能为空", trigger: "blur" }
        ],
        studentId: [
          { required: true, message: "学号不能为空", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "课程编号不能为空", trigger: "blur" }
        ],
        term: [
          { required: true, message: "学期不能为空", trigger: "blur" }
        ],
        changeReason: [
          { required: true, message: "修改原因不能为空", trigger: "blur" }
        ],
        operatorId: [
          { required: true, message: "操作人工号不能为空", trigger: "blur" }
        ],
        operateTime: [
          { required: true, message: "操作时间不能为空", trigger: "blur" }
        ],
        source: [
          { required: true, message: "来源不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询成绩修改记录列表 */
    getList() {
      this.loading = true
      listLog(this.queryParams).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        logId: null,
        scoreId: null,
        studentId: null,
        courseId: null,
        term: null,
        oldUsual: null,
        oldFinal: null,
        oldTotal: null,
        newUsual: null,
        newFinal: null,
        newTotal: null,
        changeReason: null,
        operatorId: null,
        operateTime: null,
        source: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.logId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加成绩修改记录"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const logId = row.logId || this.ids
      getLog(logId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改成绩修改记录"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.logId != null) {
            updateLog(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addLog(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const logIds = row.logId || this.ids
      this.$modal.confirm('是否确认删除成绩修改记录编号为"' + logIds + '"的数据项？').then(function() {
        return delLog(logIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/log/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
