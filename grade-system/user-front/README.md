# grade-front · 成绩管理系统前端

对应 Axure 原型的 6 个页面，用 **Vue 3 + Vite + Pinia + Vue Router** 实现，无 UI 框架依赖，样式按原型的设计规范手写。

> 本工程是课程作业里的「鼓励项」：**先完成 Axure 原型，再做前端实现**。
> 页面结构、字段、交互与 `docs/03-wireframe-spec.md` 一一对应。

---

## 一、运行

```bash
cd grade-front
npm install       # 首次运行需要（依赖已随包提供，可跳过）
npm run dev       # 开发模式，自动打开 http://localhost:5173
```

其他命令：

```bash
npm run build     # 构建到 dist/
npm run preview   # 本地预览构建产物（http://localhost:4173）
```

> ⚠️ `dist/index.html` **不能**直接双击用 `file://` 打开——浏览器会以 CORS 策略拒绝加载 ES Module。
> 请用 `npm run dev` 或 `npm run preview` 启动本地服务。

## 二、测试账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 学生 | `2021001` | `123456` |
| 学生 | `2021003` | `123456` |
| 学生 | `2021005` | `123456` |
| 班主任 | `T001` | `123456` |

登录页底部有「重置演示数据」，演示前点一下可恢复初始数据。

## 三、页面与路由

| 页面 | 路由 | 角色 | 关联需求 |
|---|---|---|---|
| P1 学生登录 | `#/login` | 公共 | FR-P1-01 ~ 07 |
| P2 成绩查询 | `#/student/scores` | 学生 | FR-P2-01 ~ 10 |
| P3 申请查分 | `#/student/appeal` | 学生 | FR-P3-01 ~ 09 |
| P6 受理查分申请 | `#/teacher/appeals` | 班主任 | FR-P6-01 ~ 10 |
| P4 添加学生成绩 | `#/teacher/scores/add` | 班主任 | FR-P4-01 ~ 09 |
| P5 修改学生成绩 | `#/teacher/scores/edit` | 班主任 | FR-P5-01 ~ 08 |

登录成功后按角色自动分流；访问无权限的路由会被守卫重定向到本角色首页。

## 四、演示路径（走完即演示完整业务闭环）

```
1. 用 2021001 / 123456 登录（学生）
2. P2 成绩查询：切换学期筛选、指出 50 分标红并带「不及格」标签
3. 点「软件工程导论」行内的「申请查分」 → 跳 P3 并自动带入该课程
4. P3 填理由（或用常用理由标签）→ 提交 → 列表新增一条「待受理」
5. 退出登录 → 用 T001 / 123456 登录（班主任）
6. P6 待受理列表出现刚才那条申请（角标为 3）
7. 点「受理」→ 选择「更正成绩」→ 填 56 → 填写处理意见 → 确认
   → 该条从待受理移到已受理
8. P5 修改学生成绩：查 2021001 + 软件工程导论 → 对照卡片显示更正后的 56 分
   → 修改记录里能看到「查分受理更正」这条留痕
9. P4 添加学生成绩：批量录入，总评随平时/期末自动计算
```

## 五、目录结构

```
grade-front/
├── index.html
├── vite.config.js
└── src/
    ├── main.js                    # 应用入口
    ├── App.vue                    # 按路由 meta.public 切换布局
    ├── router/index.js            # 路由表 + 登录守卫
    ├── stores/
    │   ├── user.js                # 登录用户、角色（对应原型全局变量 LoginUser / Role）
    │   └── score.js               # 成绩 / 查分申请 / 修改记录（唯一数据源，localStorage 持久化）
    ├── api/
    │   ├── mock.js                # 演示数据，与 docs/04-data-and-vue.md §2 一致
    │   └── request.js             # 真实接口封装占位（接口清单见 §6）
    ├── layouts/
    │   ├── DefaultLayout.vue      # 对应母版：顶部导航 + 侧边菜单 + 内容区
    │   └── BlankLayout.vue        # 登录页 / 404
    ├── components/
    │   ├── AppHeader.vue          # M_顶部导航
    │   ├── AppSidebar.vue         # M_侧边菜单（按角色渲染）
    │   ├── AppFooter.vue
    │   ├── FilterBar.vue          # ② 筛选区
    │   ├── DataTable.vue          # 通用表格（对应 Axure 中继器）
    │   ├── Pagination.vue
    │   ├── EmptyState.vue         # 空状态
    │   ├── BaseModal.vue          # 弹窗（对应 dp_受理弹窗 / dp_二次确认）
    │   ├── StatusTag.vue          # 状态标签（枚举字典见 §1.8）
    │   ├── ScoreCell.vue          # 分数着色单元格
    │   └── ToastHost.vue          # 轻提示
    ├── composables/useToast.js
    ├── utils/
    │   ├── score.js               # 总评 / 绩点 / 着色 / 等级 / 平均绩点
    │   ├── validate.js            # 校验规则（§3 校验规则汇总表）
    │   └── format.js              # 日期格式化
    └── styles/
        ├── variables.css          # 颜色 / 字号变量（与原型设计规范一致）
        └── main.css
```

## 六、实现要点

| 需求 | 实现位置 | 说明 |
|---|---|---|
| FR-P1-06 角色分流 | `router/index.js`、`stores/user.js` | 登录守卫 + `homeRoute` getter |
| FR-P2-06 分数着色 | `utils/score.js` + `ScoreCell.vue` | 颜色不是唯一信息载体，同时输出「不及格」文字标签 |
| FR-P3-08 防重复提交 | `stores/score.js` `hasPendingAppeal` | 同课程已有待受理申请则阻止提交，P2 的按钮也会置灰 |
| FR-P4-02 总评自动计算 | `ScoreAddView.vue` | 输入时即时计算，无需失焦提交 |
| FR-P5-05 二次确认 | `ScoreEditView.vue` + `BaseModal` | 弹窗内动态展示「平时 52→56，总评 50→51」 |
| FR-P5-06/07 留痕与锁定 | `stores/score.js`、`ScoreEditView.vue` | 写入 `changeLogs`；成绩处于「查分中」时整卡只读 |
| FR-P6-05 条件必填 | `AppealHandleView.vue` | 选「更正成绩」时才要求填写更正后成绩 |
| FR-P6-07 状态流转 | `stores/score.js` `handleAppeal` | 受理后移出待受理；更正成绩同步更新总评并写入修改记录 |

## 七、数据说明

- 所有数据存在浏览器 `localStorage`（键：`grade-system:user` / `grade-system:data`），刷新不丢，方便演示「学生提交 → 班主任受理」的跨角色闭环。
- `api/mock.js` 里的成绩**不写死总评与绩点**，统一由 `utils/score.js` 按公式计算，避免示例数据与规则不一致。
- 接真实后端时，替换 `api/request.js` 的实现，并把 `stores/score.js` 里的本地计算改为调用接口即可；接口清单见 `docs/04-data-and-vue.md` §6。
