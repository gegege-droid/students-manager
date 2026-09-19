# 数据字典与 Vue 3 前端落地建议

> 本文件有两个用途：
> 1. **做原型时**：Axure 里的下拉框选项、表格示例数据、中继器数据集，全部从 §1 数据字典和 §2 Mock 数据里抄，保证 6 个页面数据一致、不出戏。
> 2. **实现前端时**：作业里「鼓励有基础的组根据原型页面设计实现前端页面」——§4 给出照着本原型直接开工的 Vue 3 + Vite 工程结构、路由、组件划分。
>
> 环境已确认可用：**Node v24.18.0 / npm 11.16.0 / pnpm**。

---

## 1. 数据字典

命名约定：前端字段用 **camelCase**，接口返回用 camelCase，数据库用 snake_case（本原型不涉及数据库，仅作说明）。

### 1.1 学生 Student

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `studentId` | string | 4~12 | ✅ | 学号，主键 | `2021001` |
| `name` | string | 2~20 | ✅ | 姓名 | `张三` |
| `classId` | string | 4~16 | ✅ | 班级编号 | `SE2101` |
| `className` | string | 2~30 | ✅ | 班级名称（冗余，便于显示） | `软件工程2101班` |
| `password` | string | 6~20 | ✅ | 密码（原型明文，真实系统须加密） | `123456` |
| `gender` | enum | — | ❌ | `M` 男 / `F` 女 | `M` |
| `phone` | string | 11 | ❌ | 手机号 | `13800000001` |

### 1.2 教师 / 班主任 Teacher

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `teacherId` | string | 3~12 | ✅ | 工号，主键 | `T001` |
| `name` | string | 2~20 | ✅ | 姓名 | `李老师` |
| `password` | string | 6~20 | ✅ | 密码 | `123456` |
| `title` | string | 2~20 | ❌ | 职称 | `讲师` |
| `manageClassIds` | string[] | — | ✅ | 所带班级编号列表（权限依据） | `["SE2101","SE2102"]` |

### 1.3 课程 Course

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `courseId` | string | 4~12 | ✅ | 课程号，主键 | `CS101` |
| `courseName` | string | 2~40 | ✅ | 课程名称 | `数据结构` |
| `credit` | number | 0.5~10 | ✅ | 学分，可含 0.5 | `4` |
| `term` | string | 11 | ✅ | 学期，格式 `YYYY-YYYY-N` | `2024-2025-1` |
| `teacherId` | string | 3~12 | ✅ | 任课教师工号 | `T001` |
| `courseType` | enum | — | ❌ | `REQUIRED` 必修 / `ELECTIVE` 选修 | `REQUIRED` |

### 1.4 成绩 Score

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `scoreId` | string | 12~24 | ✅ | 成绩记录 ID，主键 | `SC20250101001` |
| `studentId` | string | 4~12 | ✅ | 学号（外键） | `2021001` |
| `courseId` | string | 4~12 | ✅ | 课程号（外键） | `CS101` |
| `term` | string | 11 | ✅ | 学期 | `2024-2025-1` |
| `usualScore` | number\|null | 0~100 | ❌ | 平时成绩，整数 | `85` |
| `finalScore` | number\|null | 0~100 | ❌ | 期末成绩，整数 | `92` |
| `totalScore` | number\|null | 0~100 | — | 总评成绩，**后端计算** | `89` |
| `gpa` | number\|null | 0~5 | — | 绩点，**后端计算**，1 位小数 | `3.9` |
| `status` | enum | — | ✅ | 见 §1.8 枚举 | `NORMAL` |
| `recorderId` | string | 3~12 | ✅ | 录入人工号 | `T001` |
| `recordTime` | datetime | — | ✅ | 录入时间 | `2025-01-10 16:20:00` |
| `remark` | string | ≤100 | ❌ | 备注 | — |

**唯一约束**：`(studentId, courseId, term)` 唯一 —— 同一学生同一课程同一学期只能有一条成绩。

### 1.5 查分申请 Appeal

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `appealId` | string | 15 | ✅ | 申请编号，主键，规则见下 | `AP20250115001` |
| `studentId` | string | 4~12 | ✅ | 申请人学号 | `2021001` |
| `courseId` | string | 4~12 | ✅ | 课程号 | `CS101` |
| `term` | string | 11 | ✅ | 学期 | `2024-2025-1` |
| `originalScore` | number | 0~100 | ✅ | 申请时的原总评（快照，**不随时间变化**） | `89` |
| `reason` | string | 10~200 | ✅ | 申请理由 | `期末成绩与预期差距较大，请老师复核` |
| `applyTime` | datetime | — | ✅ | 申请时间 | `2025-01-15 14:30:00` |
| `status` | enum | — | ✅ | `PENDING` / `ACCEPTED` / `REJECTED` | `PENDING` |
| `result` | enum\|null | — | ❌ | 处理结果 `KEEP` 维持 / `CORRECT` 更正 | `CORRECT` |
| `correctedScore` | number\|null | 0~100 | ❌ | 更正后的成绩 | `92` |
| `opinion` | string | 5~200 | ❌ | 处理意见 / 驳回理由 | `经复核确为登分错误，已更正` |
| `handlerId` | string | 3~12 | ❌ | 受理人工号 | `T001` |
| `handleTime` | datetime | — | ❌ | 受理时间 | `2025-01-16 10:22:00` |

**申请编号生成规则**：`AP` + `yyyyMMdd` + 3 位当日流水，如 `AP20250115001`。

### 1.6 成绩修改记录 ScoreChangeLog

| 字段 | 类型 | 长度 | 必填 | 说明 | 示例 |
|---|---|:---:|:---:|---|---|
| `logId` | string | 16~24 | ✅ | 记录 ID | `LOG20250116001` |
| `scoreId` | string | 12~24 | ✅ | 关联成绩 ID | `SC20250101001` |
| `studentId` | string | 4~12 | ✅ | 学号 | `2021001` |
| `courseId` | string | 4~12 | ✅ | 课程号 | `CS101` |
| `oldUsual` / `oldFinal` / `oldTotal` | number | 0~100 | ❌ | 修改前 | `85 / 92 / 89` |
| `newUsual` / `newFinal` / `newTotal` | number | 0~100 | ❌ | 修改后 | `90 / 88 / 90` |
| `changeReason` | string | 5~100 | ✅ | 修改原因 | `录入时看错行，据实更正` |
| `operatorId` | string | 3~12 | ✅ | 操作人工号 | `T001` |
| `operateTime` | datetime | — | ✅ | 操作时间 | `2025-01-16 10:22:00` |
| `source` | enum | — | ✅ | `MANUAL` 手工修改 / `APPEAL` 查分更正 | `APPEAL` |

### 1.7 登录 / 会话 Session（仅前端演示用）

| 字段 | 类型 | 说明 |
|---|---|---|
| `loginId` | string | 学号或工号 |
| `role` | enum | `STUDENT` / `TEACHER` |
| `name` | string | 显示名 |
| `loginTime` | datetime | 登录时间 |

### 1.8 枚举字典（**Axure 下拉框和状态标签直接用这张表**）

| 枚举名 | 值 | 中文 | 颜色 | 用于 |
|---|---|---|---|---|
| 角色 `Role` | `STUDENT` | 学生 | `#2B6CB0` | P1 分流 |
| | `TEACHER` | 班主任 | `#38A169` | P1 分流 |
| 成绩状态 `ScoreStatus` | `NORMAL` | 正常 | `#718096` | P2 状态列 |
| | `APPEALING` | 查分中 | `#ED8936` | P2 / P5 锁定 |
| | `MODIFIED` | 已修改 | `#2B6CB0` | P2 / P5 |
| 申请状态 `AppealStatus` | `PENDING` | 待受理 | `#ED8936` | P3 / P6 |
| | `ACCEPTED` | 已受理 | `#38A169` | P3 / P6 |
| | `REJECTED` | 已驳回 | `#E53E3E` | P3 / P6 |
| 处理结果 `AppealResult` | `KEEP` | 维持原成绩 | `#718096` | P6 弹窗单选 |
| | `CORRECT` | 更正成绩 | `#38A169` | P6 弹窗单选 |
| 课程类型 `CourseType` | `REQUIRED` | 必修 | `#E53E3E` | P4 / P5 |
| | `ELECTIVE` | 选修 | `#718096` | P4 / P5 |
| 成绩等级（展示用） | — | 优秀 ≥90 / 良好 80~89 / 中等 70~79 / 及格 60~69 / 不及格 <60 | 见 §1.9 | P2 标签 |

### 1.9 分数着色 / 等级对照

| 总评区间 | 等级 | 颜色 | 绩点 |
|---|---|---|:---:|
| 90 ~ 100 | 优秀 | `#38A169` | 4.0 ~ 5.0 |
| 80 ~ 89 | 良好 | `#38A169` | 3.0 ~ 3.9 |
| 70 ~ 79 | 中等 | `#ED8936` | 2.0 ~ 2.9 |
| 60 ~ 69 | 及格 | `#ED8936` | 1.0 ~ 1.9 |
| 0 ~ 59 | 不及格 | `#E53E3E`（加粗 + 文案标签） | 0 |

---

## 2. Mock 数据（Axure 中继器数据集 / 前端 mock 直接抄）

### 2.1 登录账号

```json
[
  { "loginId": "2021001", "password": "123456", "role": "STUDENT", "name": "张三",   "className": "软件工程2101班" },
  { "loginId": "2021002", "password": "123456", "role": "STUDENT", "name": "李四",   "className": "软件工程2101班" },
  { "loginId": "T001",    "password": "123456", "role": "TEACHER", "name": "李老师", "manageClassIds": ["SE2101"] }
]
```

### 2.2 成绩列表（P2 表格数据源，8 条）

| 序号 | 课程号 | 课程名称 | 学分 | 平时 | 期末 | 总评 | 绩点 | 状态 |
|---:|---|---|---:|---:|---:|---:|---:|---|
| 1 | CS101 | 数据结构 | 4 | 85 | 92 | 89 | 3.9 | 正常 |
| 2 | CS102 | 操作系统 | 3.5 | 78 | 82 | 80 | 3.0 | 正常 |
| 3 | CS103 | 计算机网络 | 3 | 66 | 72 | 70 | 2.0 | 正常 |
| 4 | CS104 | 数据库原理 | 3.5 | 58 | 64 | 62 | 1.2 | 正常 |
| 5 | MA201 | 概率论与数理统计 | 3 | 91 | 95 | 93 | 4.3 | 正常 |
| 6 | EN101 | 大学英语（三） | 2 | 88 | 86 | 87 | 3.7 | 正常 |
| 7 | PE101 | 体育（三） | 1 | 95 | 90 | 92 | 4.2 | 正常 |
| 8 | CS105 | 软件工程导论 | 2 | 52 | 48 | 50 | 0 | **不及格** |

> 汇总：已修课程 8 门 · 总学分 22 · 平均分 77.9 · 平均绩点 2.76（= 60.8 / 22，按 §5.3 学分绩点公式计算）。
> **建议把第 8 条设成不及格**，这样答辩时「点行内申请查分」的动机就非常自然。

### 2.3 我的查分申请（P3 列表数据源，3 条）

| 申请编号 | 课程 | 原成绩 | 申请理由 | 申请时间 | 状态 | 处理结果 |
|---|---|---:|---|---|---|---|
| AP20250115001 | 软件工程导论 | 50 | 平时分已按要求提交全部作业，请老师复核登分 | 2025-01-15 14:30 | 待受理 | — |
| AP20241220003 | 数据结构 | 89 | 期末成绩与预期差距较大 | 2024-12-20 09:15 | 已受理 | 维持原成绩：经复核无误 |
| AP20241218001 | 大学英语（三） | 87 | 听力部分疑似未计入总分 | 2024-12-18 16:40 | 已驳回 | 复核后确认计分无误 |

### 2.4 学生名单（P4 录入表格数据源，示例 6 条）

| 序号 | 学号 | 姓名 | 平时 | 期末 | 总评 | 状态 |
|---:|---|---|---:|---:|---:|---|
| 1 | 2021001 | 张三 | 85 | 92 | 89 | 已录入 |
| 2 | 2021002 | 李四 | | | — | 未录入 |
| 3 | 2021003 | 王五 | 76 | 81 | 79 | 已录入 |
| 4 | 2021004 | 赵六 | | | — | 未录入 |
| 5 | 2021005 | 钱七 | | | — | 未录入 |
| 6 | 2021006 | 孙八 | | | — | 未录入 |

### 2.5 修改记录（P5 记录表格数据源，2 条）

| 修改时间 | 操作人 | 课程 | 改动内容 | 修改原因 | 结果 |
|---|---|---|---|---|---|
| 2025-01-16 10:22 | T001 李老师 | 软件工程导论 | 总评 50 → 56 | 平时分漏计一次作业，据实更正 | 已生效 |
| 2025-01-08 15:05 | T001 李老师 | 数据库原理 | 期末 60 → 64，总评 62 → 62 | 登分时看错行 | 已生效 |

### 2.6 待受理申请（P6 表格数据源，3 条）

| 申请编号 | 学号 | 姓名 | 课程 | 原成绩 | 申请时间 | 申请理由 |
|---|---|---:|---|---:|---|---|
| AP20250115001 | 2021001 | 张三 | 软件工程导论 | 50 | 2025-01-15 14:30 | 平时分已按要求提交全部作业，请老师复核登分 |
| AP20250115002 | 2021003 | 王五 | 计算机网络 | 70 | 2025-01-15 15:10 | 期末卷面估分 78，与公布成绩差距较大 |
| AP20250115003 | 2021005 | 钱七 | 数据库原理 | 62 | 2025-01-15 18:45 | 平时分一项显示为空，疑似漏录 |

---

## 3. 校验规则汇总表（前端与原型共用）

| 字段 | 规则（正则 / 条件） | 错误文案 |
|---|---|---|
| 账号 | `^[A-Za-z0-9]{6,12}$` | 请输入学号或工号（6~12 位字母或数字） |
| 密码 | 长度 6~20 | 请输入密码（6~20 位） |
| 验证码 | 长度 4 | 请输入验证码 |
| 成绩分数 | `^\d{1,3}$` 且 0 ≤ v ≤ 100 | 成绩必须是 0~100 的整数 |
| 申请理由 | `trim().length` 在 10~200 | 请填写 10~200 字的申请理由 |
| 修改原因 | `trim().length` 在 5~100 | 请填写修改原因（5~100 字） |
| 处理意见 | `trim().length` 在 5~200 | 请填写处理意见（5~200 字） |
| 更正后成绩 | 选择「更正成绩」时必填，0~100 整数 | 请填写更正后的成绩（0~100） |
| 关键字搜索 | 长度 ≤ 30 | 搜索关键字过长 |

**校验时机**：失焦时校验单字段 → 提交时全量校验 → 校验失败聚焦到第一个错误字段。

---

## 4. Vue 3 + Vite 前端落地建议（鼓励项）

> 前提：**先完成 Axure 原型**。原型定稿后再写前端，避免页面结构反复改。
> 本节的目录结构、路由、组件划分与 `03-wireframe-spec.md` 的 6 个页面**一一对应**。

### 4.1 初始化工程

```bash
cd /d D:\work
npm create vite@latest grade-front -- --template vue
cd grade-front
npm install
npm install vue-router@4 pinia
npm run dev
```

访问 `http://localhost:5173`。构建产物：`npm run build` → `dist/`。

> 构建产物用 `npm run preview` 本地预览即可（默认 `http://localhost:4173`）。
>
> ⚠️ **注意**：`dist/index.html` **不能**直接双击用 `file://` 打开 —— 浏览器会以 CORS 策略拒绝加载 ES Module，页面会白屏。这不是代码问题，是浏览器的安全限制。
> 如果确实需要「一个文件双击就能演示」，可以装 `vite-plugin-singlefile` 把 JS/CSS 全部内联进 HTML；或者更简单：提交时附上 `npm run dev` 的操作说明。

### 4.2 目录结构

```
grade-front/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.js
    ├── App.vue
    ├── router/
    │   └── index.js              # 路由表 + 登录守卫
    ├── stores/
    │   ├── user.js               # Pinia：登录用户、角色（对应 Axure 全局变量）
    │   └── score.js              # 成绩与申请数据（可先用 mock）
    ├── api/
    │   ├── mock.js               # §2 的示例数据，前端先用这套跑通
    │   └── request.js            # 预留 axios 封装，后续换真接口
    ├── layouts/
    │   ├── DefaultLayout.vue     # 对应母版：顶栏 + 侧边菜单 + 内容区插槽
    │   └── BlankLayout.vue       # 登录页专用（无框架）
    ├── components/
    │   ├── AppHeader.vue         # 对应 M_顶部导航
    │   ├── AppSidebar.vue        # 对应 M_侧边菜单（按角色渲染菜单项）
    │   ├── AppFooter.vue
    │   ├── FilterBar.vue         # 对应「② 筛选区」
    │   ├── DataTable.vue         # 通用表格（对应 Axure 中继器）
    │   ├── Pagination.vue
    │   ├── EmptyState.vue        # 对应空状态组合
    │   ├── BaseModal.vue         # 对应 dp_受理弹窗 / dp_二次确认
    │   ├── BaseToast.vue         # 对应 M_提示_成功
    │   ├── StatusTag.vue         # 状态标签（按枚举出颜色）
    │   └── ScoreCell.vue         # 分数着色单元格
    ├── views/
    │   ├── LoginView.vue             # P1
    │   ├── student/
    │   │   ├── ScoreQueryView.vue    # P2
    │   │   └── AppealApplyView.vue   # P3
    │   └── teacher/
    │       ├── ScoreAddView.vue      # P4
    │       ├── ScoreEditView.vue     # P5
    │       └── AppealHandleView.vue  # P6
    ├── utils/
    │   ├── score.js              # 总评 / 绩点 / 着色 计算，对应 §5.3 公式
    │   └── validate.js           # §3 校验规则
    └── styles/
        ├── variables.css         # §2.1 的颜色 / 字号变量
        └── main.css
```

### 4.3 路由表

| path | name | 组件 | 角色 | 对应页面 |
|---|---|---|---|---|
| `/login` | `Login` | `LoginView` | 公共 | P1 |
| `/` | — | 重定向到 `/login` | — | — |
| `/student/scores` | `StudentScoreQuery` | `ScoreQueryView` | STUDENT | P2 |
| `/student/appeal` | `StudentAppealApply` | `AppealApplyView` | STUDENT | P3 |
| `/teacher/scores/add` | `TeacherScoreAdd` | `ScoreAddView` | TEACHER | P4 |
| `/teacher/scores/edit` | `TeacherScoreEdit` | `ScoreEditView` | TEACHER | P5 |
| `/teacher/appeals` | `TeacherAppealHandle` | `AppealHandleView` | TEACHER | P6 |
| `/:pathMatch(.*)*` | `NotFound` | `NotFoundView` | — | — |

**登录守卫逻辑**

```js
router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.public) return true
  if (!user.loginId) return { name: 'Login' }
  // 角色不匹配 → 重定向到该角色首页
  if (to.meta.role && to.meta.role !== user.role) {
    return { name: user.role === 'STUDENT' ? 'StudentScoreQuery' : 'TeacherAppealHandle' }
  }
  return true
})
```

### 4.4 Axure 元件 → Vue 组件映射表

> 这张表是「原型驱动开发」的关键：Axure 里做的每个元件，在前端都有一个确定的落点，不会出现「原型和代码对不上」。

| Axure 元件 / 母版 | Vue 落点 | 说明 |
|---|---|---|
| `M_顶部导航` | `components/AppHeader.vue` | 用户信息取 `stores/user` |
| `M_侧边菜单_学生` / `_教师` | `components/AppSidebar.vue` | 按 `role` 渲染不同菜单数组 |
| `M_页脚` | `components/AppFooter.vue` | — |
| `M_弹窗_确认` | `components/BaseModal.vue` | 通过 props 传标题与内容 |
| `M_提示_成功` | `components/BaseToast.vue` | 全局方法 `toast.success('保存成功')` |
| `区域_筛选` | `components/FilterBar.vue` | 插槽式，各页面自定义筛选项 |
| 中继器 `tb_成绩列表` | `components/DataTable.vue` | `columns` + `rows` props，支持作用域插槽渲染自定义单元格 |
| `dp_空状态` | `components/EmptyState.vue` | props：主文案 / 辅助文案 |
| `dp_受理弹窗` | `AppealHandleView` 内的 `BaseModal` | — |
| `dp_二次确认` | `BaseModal` + `ElMessageBox` 风格 | — |
| 全局变量 `LoginUser` / `Role` | Pinia `stores/user.js` | — |
| 全局变量 `SelectedCourse` | 路由 query：`/student/appeal?courseId=CS101` | **比全局变量更可靠，刷新不丢** |
| 中继器数据集 | `api/mock.js` | 后续换成 `api/request.js` 调后端 |
| 元件注释 FR 编号 | 组件顶部注释 / 单元测试名 | 保持需求可追溯 |

### 4.5 核心工具函数（对应 §5.3 公式，直接可用）

```js
// src/utils/score.js

/** 总评 = 平时 × 0.4 + 期末 × 0.6，四舍五入取整 */
export function calcTotal(usualScore, finalScore) {
  if (usualScore == null || finalScore == null) return null
  return Math.round(usualScore * 0.4 + finalScore * 0.6)
}

/** 绩点 = 总评 < 60 ? 0 : (总评 - 50) / 10，保留 1 位小数 */
export function calcGpa(totalScore) {
  if (totalScore == null) return null
  return totalScore < 60 ? 0 : Number(((totalScore - 50) / 10).toFixed(1))
}

/** 分数颜色：>=80 绿，60~79 橙，<60 红 */
export function scoreColor(totalScore) {
  if (totalScore == null) return '#718096'
  if (totalScore >= 80) return '#38A169'
  if (totalScore >= 60) return '#ED8936'
  return '#E53E3E'
}

/** 分数等级文案 */
export function scoreLevel(totalScore) {
  if (totalScore == null) return '—'
  if (totalScore >= 90) return '优秀'
  if (totalScore >= 80) return '良好'
  if (totalScore >= 70) return '中等'
  if (totalScore >= 60) return '及格'
  return '不及格'
}

/** 学分绩点：Σ(绩点 × 学分) / Σ学分 */
export function calcAvgGpa(scores) {
  const valid = scores.filter((s) => s.gpa != null && s.credit)
  const creditSum = valid.reduce((sum, s) => sum + s.credit, 0)
  if (!creditSum) return 0
  return Number((valid.reduce((sum, s) => sum + s.gpa * s.credit, 0) / creditSum).toFixed(2))
}
```

### 4.6 样式变量（对应 §2.1 颜色规范，让前端与原型视觉一致）

```css
/* src/styles/variables.css */
:root {
  --color-primary: #2b6cb0;
  --color-primary-light: #ebf4ff;
  --color-success: #38a169;
  --color-warning: #ed8936;
  --color-danger: #e53e3e;
  --color-text: #1a202c;
  --color-text-secondary: #718096;
  --color-border: #e2e8f0;
  --color-bg: #f7fafc;
  --color-card: #ffffff;

  --font-size-title: 20px;
  --font-size-section: 16px;
  --font-size-body: 14px;
  --font-size-hint: 12px;

  --radius-card: 8px;
  --page-width: 1440px;
  --sidebar-width: 240px;
  --header-height: 64px;
}
```

### 4.7 实现顺序建议

| 阶段 | 内容 | 产出 |
|---|---|---|
| 1 | 初始化工程 + 样式变量 + Pinia + 路由骨架 | 能跑起来，路由能跳 |
| 2 | `DefaultLayout` + `AppHeader` + `AppSidebar` | 框架与 Axure 母版视觉一致 |
| 3 | `LoginView` + 路由守卫 | P1 可用，能按角色进不同首页 |
| 4 | `utils/score.js` + `DataTable` + `EmptyState` + `StatusTag` | 通用能力就绪 |
| 5 | `ScoreQueryView` | P2 可用（筛选 + 着色 + 跳转） |
| 6 | `AppealApplyView` | P3 可用（提交 + 列表刷新） |
| 7 | `AppealHandleView` | P6 可用（受理 → 状态流转） |
| 8 | `ScoreAddView` | P4 可用（自动算总评） |
| 9 | `ScoreEditView` | P5 可用（二次确认 + 修改记录） |
| 10 | 全流程联调 + 统一视觉 + 构建 | `npm run build` 产出 `dist/` |

### 4.8 加分小技巧

| 技巧 | 说明 |
|---|---|
| `localStorage` 持久化 | 把 user 与申请数据存 `localStorage`，刷新不丢，演示更可信 |
| 打印样式 | 成绩查询页加 `@media print`，可导出成绩单 PDF，超出作业要求 |
| 分数分布图 | P2 或 P6 加一个班级分数分布柱状图（ECharts / 纯 CSS 柱条） |
| 骨架屏 | 表格加载时显示灰色占位条，体现交互细节 |
| 键盘可达 | 登录页回车提交、弹窗 ESC 关闭、焦点自动定位到第一个输入框 |

---

## 5. 需求 → 页面 → 组件 追溯矩阵

> 需求规格说明书里放这张表，就是「需求可追溯性」的证据。

| 需求编号 | 需求摘要 | 页面 | Axure 元件 | Vue 组件 |
|---|---|---|---|---|
| FR-P1-01~07 | 登录与角色分流 | P1 | `btn_登录`、`dp_登录错误` | `LoginView` |
| FR-P2-01~10 | 成绩查询与筛选 | P2 | `tb_成绩列表`、`ddl_学期` | `ScoreQueryView`、`DataTable`、`ScoreCell` |
| FR-P3-01~09 | 查分申请与进度 | P3 | `ipt_申请理由`、`tb_我的申请` | `AppealApplyView` |
| FR-P4-01~09 | 批量录入成绩 | P4 | `tb_录入列表`、`btn_保存` | `ScoreAddView` |
| FR-P5-01~08 | 修改成绩与留痕 | P5 | `dp_对照卡片`、`tb_修改记录`、`dp_二次确认` | `ScoreEditView`、`BaseModal` |
| FR-P6-01~10 | 受理查分申请 | P6 | `dp_申请列表`、`dp_受理弹窗` | `AppealHandleView` |

---

## 6. 如果要接真实后端（可选，写进说明书即可）

| 接口 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 登录 | POST | `/api/auth/login` | 入参 loginId + password，返回 token 与用户信息 |
| 查询成绩 | GET | `/api/scores?term=&keyword=` | 学生查自己，教师查本班 |
| 提交查分申请 | POST | `/api/appeals` | 入参 studentId、courseId、reason |
| 我的申请 | GET | `/api/appeals/mine` | 学生视角 |
| 录入成绩 | POST | `/api/scores/batch` | 批量保存 |
| 修改成绩 | PUT | `/api/scores/:scoreId` | 必须带 changeReason |
| 待受理列表 | GET | `/api/appeals?status=PENDING` | 教师视角 |
| 受理申请 | PUT | `/api/appeals/:appealId/handle` | 入参 result、correctedScore、opinion |

**后端必须做的校验（不能只靠前端）**：角色权限、成绩范围 0~100、唯一约束 `(studentId, courseId, term)`、成绩状态为 `APPEALING` 时禁止修改、受理后同步更新成绩表与修改记录。
