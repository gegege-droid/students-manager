# 文件架构与系统设计

> 本文说明交付包的整体文件结构、前端工程的分层与依赖方向、以及一次「查分受理」在代码里的完整数据流。
> 用于需求规格说明书的「软件结构设计」章节，也方便其他人接手改代码。

---

## 1. 交付包整体结构

```
ruanjian/
├── README.md                     交付包总览：文件清单、快速上手、假设与待确认项
├── wireframes.html               可视化线框图（公共框架 + 6 页 + 弹窗放样），可打印成 PDF
│
├── docs/                         ← 设计文档（交付/说明书素材）
│   ├── 01-requirements.md        需求分析：作业对照表、权限矩阵、FR 需求表、状态机、假设
│   ├── 02-axure-guide.md         Axure 制作指导：母版、交互清单、变量表达式、发布导出
│   ├── 03-wireframe-spec.md      六页线框说明：区域、元件、字段校验、交互、状态
│   ├── 04-data-and-vue.md        数据字典、Mock 数据、Vue 落地建议、需求追溯矩阵
│   └── 05-architecture.md        本文：文件架构与系统设计
│
└── grade-front/                  ← 可运行的前端工程（Vue 3 + Vite）
    ├── README.md                 运行方式、测试账号、路由表、演示路径
    ├── index.html                应用宿主页（唯一 HTML 入口）
    ├── vite.config.js            构建配置：@ 别名、base、dev server
    ├── package.json              依赖与脚本（dev / build / preview）
    ├── .gitignore
    ├── dist/                     构建产物（自动生成，可提交用于演示）
    └── src/                      源码（见 §2）
```

**两份说明文档的边界**：`docs/` 面向**设计与交付**（原型怎么搭、需求是什么），`grade-front/README.md` 面向**运行与演示**（怎么跑起来、怎么走一遍）。

---

## 2. 前端分层架构

```
┌──────────────────────────────────────────────────────────────┐
│  ① 视图层  src/views/                                          │
│     LoginView · ScoreQueryView · AppealApplyView               │
│     ScoreAddView · ScoreEditView · AppealHandleView · NotFound │
│     职责：页面布局、表单状态、校验时机、调用 store              │
└───────────────────────────┬──────────────────────────────────┘
                            │ 读取 / 调用 action
┌───────────────────────────▼──────────────────────────────────┐
│  ② 状态层  src/stores/            ← 全站唯一数据源              │
│     user.js   登录会话、角色（对应原型全局变量 LoginUser/Role） │
│     score.js  成绩、查分申请、修改记录 + 全部业务规则与状态机   │
│     职责：数据读写、业务约束、localStorage 持久化               │
└───────────────────────────┬──────────────────────────────────┘
                            │ 依赖
┌───────────────────────────▼──────────────────────────────────┐
│  ③ 数据层  src/api/                                           │
│     mock.js     演示数据（种子数据，与 04 文档数据字典一致）    │
│     request.js  真实接口封装占位（接后端时替换，视图层不动）    │
└──────────────────────────────────────────────────────────────┘

        ④ 布局层  src/layouts/    DefaultLayout（顶栏+侧栏+内容区）· BlankLayout
        ⑤ 组件层  src/components/ DataTable · BaseModal · StatusTag · ScoreCell
                                  EmptyState · Pagination · FilterBar · ToastHost
        ⑥ 工具层  src/utils/      score（公式）· validate（规则）· format（日期）
        ⑦ 样式层  src/styles/     variables.css（设计令牌）· main.css（通用类）
        ⑧ 横切    src/composables/useToast.js（全局轻提示队列）
```

### 依赖方向（单向，不允许反向 import）

| 层 | 可以依赖 | 禁止 |
|---|---|---|
| views | layouts、components、composables、stores、utils | 不直接 import `api/mock`（数据统一从 store 取） |
| layouts（DefaultLayout/BlankLayout） | components | — |
| components（AppHeader/AppSidebar） | stores（仅读 `user`） | 不写业务数据 |
| components（DataTable/BaseModal/StatusTag/ScoreCell/EmptyState/Pagination/FilterBar/ToastHost） | 无（纯 props / slots / emits） | **不 import store**，保证可复用 |
| stores | api、utils | 不 import 任何 .vue |
| utils | 无 | 纯函数、无副作用、不碰 DOM 与 store |

> `utils` 是纯函数层（`calcTotal` / `validateAppealReason` 等）， поэтому它的规则可以被单元测试，也方便原型与前端共用同一套口径。

---

## 3. 目录 → 原型元件 → 需求 的映射

> 这张表是「原型驱动开发」的落点：Axure 里画的每个母版/元件，在代码里都有确定的位置。

| Axure 母版 / 元件 | 代码位置 | 关联需求 |
|---|---|---|
| `M_顶部导航` | `components/AppHeader.vue` | FR-P1-07 |
| `M_侧边菜单_学生` / `_教师` | `components/AppSidebar.vue`（按 `role` 渲染） | 权限矩阵 §2.2 |
| `M_页脚` | `components/AppFooter.vue` | — |
| `M_弹窗_确认`、`dp_受理弹窗`、`dp_二次确认` | `components/BaseModal.vue` | FR-P5-05、FR-P6-03 |
| `M_提示_成功` | `components/ToastHost.vue` + `composables/useToast.js` | FR-P3-06、FR-P4-07 |
| `区域_筛选` | `components/FilterBar.vue` | FR-P2-02、FR-P6-02 |
| 中继器 `tb_成绩列表` / `tb_我的申请` / `tb_待受理` … | `components/DataTable.vue` + 各视图的 `#cell` 插槽 | FR-P2-04、FR-P3-07、FR-P6-02 |
| `dp_空状态` / `dp_无待办` / `dp_未选择` | `components/EmptyState.vue` | FR-P2-08、FR-P3-09、FR-P6-09 |
| 状态标签（枚举字典 §1.8） | `components/StatusTag.vue` | FR-P2-06、FR-P6-07 |
| 总评成绩单元格（分数着色） | `components/ScoreCell.vue` | FR-P2-06 |
| 全局变量 `LoginUser` / `Role` | `stores/user.js` | FR-P1-06 |
| 全局变量 `SelectedCourse` | 路由 query：`/student/appeal?courseId=CS101` | FR-P2-09 |
| 中继器数据集 | `api/mock.js` → `stores/score.js` | — |
| `区域_统计`（已修课程 / 总学分 / 平均分 / 平均绩点） | `views/student/ScoreQueryView.vue` 的 `stats` computed | FR-P2-07 |
| 统一评分公式 | `utils/score.js` | §5.3 |
| 校验文案与规则 | `utils/validate.js` | §3 校验规则表 |

---

## 4. 路由与页面容器

```
App.vue
 ├─ 按 route.meta.public 选择布局
 │    ├─ true  → BlankLayout   → LoginView / NotFoundView
 │    └─ false → DefaultLayout → AppHeader + AppSidebar + <slot> + AppFooter
 │
 └─ <router-view>  渲染当前页面
      #/login                      P1  公共
      #/student/scores             P2  role=STUDENT
      #/student/appeal             P3  role=STUDENT
      #/teacher/appeals            P6  role=TEACHER
      #/teacher/scores/add         P4  role=TEACHER
      #/teacher/scores/edit        P5  role=TEACHER
      #/:pathMatch(.*)*            404
```

**路由守卫（`router/index.js`）三件事**：未登录 → 去登录页；已登录访问登录页 → 去本角色首页；角色不符 → 重定向回本角色首页。这是权限从「原型里的菜单差异」变成「真实拦截」的地方。

---

## 5. 数据流：一条查分申请走完全程

> 理解这条链路就理解了整个工程。核心设计是 **单一数据源 + 一个 action 内改完所有相关数据 + 立即持久化**。

```
① 学生登录（P1 LoginView）
   form → validate()（utils/validate）
        → user.login()        stores/user.js：写 loginId/role/name → localStorage
        → router.push(user.homeRoute)   按角色分流

② 提交查分申请（P3 AppealApplyView）
   route.query.courseId  → 预选课程（从 P2 带来的 SelectedCourse）
   form.reason           → validateAppealReason()
   重复校验              → data.hasPendingAppeal()        ← 业务规则在 store，不在视图
        → data.submitAppeal()  stores/score.js：
              appeals.unshift(新申请, status='PENDING') → persist()
        → myAppeals（computed）自动重算 → DataTable 重新渲染
        → 提交成功弹窗（BaseModal）

③ 班主任查看待办（P6 AppealHandleView）
   data.appealRowsByStatus('PENDING')
        → decorateAppeal()：拼上学生姓名、课程名（跨表字段在这里补全，视图不做关联）

④ 受理（P6 弹窗确认）
   validateOpinion() / 条件必填校验（选「更正成绩」才要求分数）
        → data.handleAppeal(appealId, { result, correctedScore, opinion })
              这**一个** action 里同时改三处：
              ├─ appeals[]  → status='ACCEPTED'、result、opinion、handlerId、handleTime
              ├─ scores[]   → totalScore=更正分、gpa 重算、manualTotal=true
              └─ logs.unshift(留痕) → 记录原值/新值/原因/操作人/时间/source='APPEAL'
              → persist()（一次写盘）

⑤ 联动效果（无需任何额外代码，全部由数据驱动）
   P6  待受理列表少一条、角标 -1；已受理列表多一条
   P2  该课程状态列由「查分中」变为「已更正」（scoreStatusOf 依据 appeals + logs 计算）
   P5  修改记录表格出现该条「查分受理更正」
   P3  学生端「查看回复」能看到处理结果、更正后成绩与处理意见
```

**三个关键设计决策**

| 决策 | 原因 |
|---|---|
| 业务规则全部下沉到 `stores/score.js` | 视图只负责「收集输入 + 展示结果」，规则（防重复申请、查分中锁定、状态推导）只有一处实现，P2/P3/P5/P6 共享 |
| `totalScore` / `gpa` **不存种子数据**，由 `utils/score.js` 计算 | 避免「示例数据与公式不一致」——前端与 Axure 原型、说明书三者口径统一 |
| 成绩状态 `scoreStatusOf` 用**推导**而非存储字段 | 状态是 `appeals`/`logs` 的函数，不存在「状态字段和实际数据不同步」的可能 |

---

## 6. 命名与代码约定

| 约定 | 示例 |
|---|---|
| 视图文件名 `XxxView.vue`，与路由名一一对应 | `ScoreQueryView.vue` ↔ `StudentScoreQuery` |
| 通用组件前缀：`App*` 为框架级，`Base*` 为可复用基础组件 | `AppHeader` / `BaseModal` |
| 页面文件注释首行标注需求编号 | `/** P3 申请查分 —— 对应 FR-P3-01 ~ FR-P3-09 */` |
| 每个校验函数返回**错误文案**，空字符串代表通过 | `validateAppealReason(value) → '请填写 10~200 字的申请理由'` |
| 常量集中：枚举与状态文案在 `StatusTag.vue`，公式在 `utils/score.js` | — |
| 样式：设计令牌放 `variables.css`，跨页通用类放 `main.css`，页面私有样式用 `<style scoped>` | `var(--color-primary)` |

---

## 7. 扩展点

| 想做什么 | 改哪里 | 视图层要不要动 |
|---|---|---|
| 接真实后端 | 实现 `api/request.js`，把 `stores/score.js` 里的本地计算改为调接口 | 不用 |
| 新增一个页面 | `views/` 加文件 + `router/index.js` 加路由 + `AppSidebar.vue` 加菜单项 | 仅新页面 |
| 改评分权重（如 30% / 70%） | `utils/score.js` 的 `calcTotal` | 不用 |
| 改状态枚举文案或配色 | `components/StatusTag.vue` 的 `MAP` | 不用 |
| 换一套主题色 | `styles/variables.css` | 不用 |
| 增加「多级审批」流程 | `stores/score.js` 的 `handleAppeal` + `appeals` 状态机 | P6 弹窗 |
| 加单元测试 | 直接测 `utils/score.js` / `utils/validate.js`（纯函数） | 不用 |
