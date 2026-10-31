# 软件工程实验二、实验三 交付说明

本目录是「高中成绩管理系统（新高考 3+1+2）」的两次实验交付物，配套项目仓库：
<https://github.com/gegege-droid/students-manager>（`grade-front/` 为可运行前端实现）。

## 一、交付清单

| 文件 / 目录 | 说明 |
|---|---|
| `实验二_概要设计说明书.docx` | 实验二交付物。10 章、6 幅图、28 张表，含系统架构模块框图、E-R 图、24 个前后端接口、模块功能说明与若依框架映射 |
| `实验三_详细设计说明书.docx` | 实验三交付物。13 章、9 幅图、30 余张表，**正文 58 页**，覆盖 6 个模块的代码文件结构、函数调用关系、处理流程、函数说明表与伪代码 |
| `sql/grade_manage.sql` | MySQL 8.0 建库建表脚本：10 张表（含注释、索引、唯一约束）+ 基础演示数据 |
| `sql/demo_data.sql` | 完整演示数据：216 条成绩（6 人 × 6 科 × 6 次）+ 36 条核算结果，与前端数值逐项一致 |
| `ruoyi/backend/` | **RuoYi-Vue 3.9.2 后端源码**（springboot3 分支，Spring Boot 3.5.16），已跑通；含用代码生成器产出的成绩管理代码 |
| `ruoyi/ui/` | **RuoYi-Vue3 前端源码**（Vue 3 + Element Plus），已跑通，新增「成绩管理 → 月考成绩」页面 |
| `tools/redis/` | Redis 8.10.3（Windows x64），若依所需的验证码/令牌缓存服务 |
| `backend/` | **后端参考实现**（Spring Boot 3 + MySQL + Swagger）：21 个接口已实现并实测通过，详见 `backend/README.md` |
| `grade-front/` | 可运行的前端工程源码（Vue 3 + Vite，8 个页面） |
| `start-ruoyi.ps1` | **一键启动若依+Redis+若依前端**（推荐） |
| `start-backend.ps1` / `start-frontend.ps1` | 启动参考后端 / 业务前端 |
| `RUNBOOK-若依环境.md` | 若依 + Redis 的部署、配置改动、代码生成器实操记录与排错 |
| `assets/*.png` | 两本文档全部插图的源文件 |

两本说明书均按课程要求排版：A4、**正文宋体小四（12 pt）、1.5 倍行距**、首行缩进 2 字符、章起新页、
页码区分封面（无）／目录（罗马数字）／正文（阿拉伯数字），目录页码为实际渲染后的真实页码。

## 二、本机实际运行状态（2026-10-31 实测）

| 组件 | 地址 | 状态 |
|---|---|---|
| **若依后端**（RuoYi-Vue 3.9.2 / Spring Boot 3.5.16 / JDK 21） | <http://localhost:8080> | 运行中，含生成的成绩管理 CRUD |
| **若依前端**（Vue 3 + Vite + Element Plus） | <http://localhost:1024> | 运行中，菜单「成绩管理 → 月考成绩」可用 |
| **Redis** 8.10.3 | `127.0.0.1:6379` | 运行中，验证码/令牌缓存生效 |
| MySQL 8.4.9（本机 `MySQL84` 服务） | `127.0.0.1:3306` | 库 `ry-vue`（若依 31 表 + 业务 9 表）、库 `grade_manage`（业务 10 表 + 222 条成绩） |
| 业务前端（Vite） | <http://localhost:5173/> | 用 `start-frontend.ps1` 启动，8 个页面可操作 |
| 参考后端 + Swagger | <http://localhost:8080>（参考实现需另起端口） | 21 个接口，详见 `backend/README.md` |

**登录账号**：若依管理端 `admin / admin123`；业务前端 学生 `20230101 / 123456`、班主任 `T001 / 123456`。

若依侧的关键结果（实测）：

| 项目 | 结果 |
|---|---|
| ruoyi-generator 生成 biz_score 管理端 | 产出 8 个文件（Controller/Service/Mapper/XML/domain/Vue/api/菜单 SQL），已落位并编译通过 |
| 生成的 CRUD 接口 | 列表 `total=222`；条件查询、新增、修改、删除均返回 200 |
| 菜单 | `sys_menu` 2000~2006（成绩管理目录 + 月考成绩菜单 + 5 个按钮权限），`/getRouters` 已下发 `/business` |
| Redis 生效证据 | 登录验证码存于 `captcha_codes:*`，从 Redis 读取后登录成功；`DBSIZE` > 0 |

详见 [RUNBOOK-若依环境.md](RUNBOOK-若依环境.md)。

业务前端与参考后端数据的一致性（实测）：

| 项目 | 前端页面 | 后端接口 |
|---|---|---|
| 林晓六次总分 | 642 / 649 / 644 / 668 / 665 / 670 | `GET /business/score/matrix` 返回同值 |
| 预测总分 | 661（区间 634~688，置信度高） | `GET /business/predict/student/20230101` 返回同值 |
| 班级/年级排名 | 第 1 名 / 第 15 名 | `biz_score_total` 同值 |
| 查分闭环 | 提交→受理→更正→留痕 | `POST /appeal` + `PUT /appeal/handle` 实测通过 |

## 三、如何启动

```powershell
# 前端（默认 http://localhost:5173）
.\start-frontend.ps1

# 后端（默认 http://localhost:8080，需 MySQL 已启动并已导入 sql）
.\start-backend.ps1
```

首次跑后端需先建库：

```powershell
mysql -uroot -proot -e "source sql/grade_manage.sql"
mysql -uroot -proot -e "source sql/demo_data.sql"
```

## 四、实验二（概要设计）要求对照

| 实验要求 | 文档位置 | 落实方式 |
|---|---|---|
| 画出系统架构模块框图 | 图 3-1「系统分层架构与模块框图」 | 用户层／表示层／控制层／状态层／数据访问层／服务端／数据存储 七层，逐层标注模块与文件；表 3-1 逐层职责与依赖约束 |
| 数据库表关系图 | 图 5-1「系统 E-R 图」 | 9 个实体、主外键与 1:1／1:N 基数；第 5 章给出表清单、`biz_score` 物理结构表、索引与数据量估算 |
| 设计前后端接口说明 | 第 6 章 | 24 个接口清单（方法／路径／功能／权限标识／参数）、前端封装映射表、典型接口 JSON 与事务说明、17 条错误码；第 9.2 节说明 Swagger 导出方式（现已可直接打开 Swagger UI 对照） |
| 各模块的功能说明 | 第 4 章、第 9 章 | 功能模块图（图 4-1）、8 个模块职责与文件映射表、15 条关键业务规则表、模块协作泳道图（图 4-2）；第 9 章给出与 RuoYi-Vue 分层的逐项映射及代码生成器用法 |

## 五、实验三（详细设计）要求对照

| 实验要求 | 文档位置 | 落实方式 |
|---|---|---|
| 至少 3 个模块的详细设计 | 第 4~9 章（共 6 个模块） | M1 登录与权限、M2 成绩录入、M3 成绩查询统计、M4 查分申请与受理、M5 成绩修改与留痕、M6 高考成绩预测 |
| 文档不少于 15 页 | 全文 | 正文 58 页（小四、1.5 倍行距），每模块 6~8 页 |
| 描述代码文件结构 | 每模块「代码文件结构」节 + 图 2-1 | 逐文件列出路径、行数与职责 |
| 描述函数或方法的调用关系 | 每模块「函数调用关系」节 | 调用关系表（调用方／被调用方／触发时机），共 6 张 |
| 描述具体实现过程（伪代码、流程图、表格） | 每模块「处理流程」「函数说明」「关键算法与伪代码」节 | 9 幅流程图／状态机图、6 张函数说明表（输入、输出、异常、边界）、14 段可直接编码的伪代码 |
| 参照 ruoyi-generator 生成代码撰写 | 第 9.3 节、第 11 章 | 给出 `biz_score` 等表的代码生成步骤、生成物在若依工程中的落位、单表 upsert 与事务实现的 Java 伪代码 |
| 详细到别的语言程序员可据此编码 | 全文 | 每个函数给出签名、输入输出、异常与边界；核心算法给出公式、伪代码与数值复算（9.8 节复算到 661 分） |
| 设计数据库表并用代码生成器生成 | `sql/grade_manage.sql`、第 11 章 | 10 张表全部带中文注释与索引，字段风格对齐若依约定，可直接导入代码生成器 |

## 六、一键启动

```powershell
.\start-ruoyi.ps1        # 若依 + Redis + 若依前端（8080 / 6379 / 1024）
.\start-frontend.ps1     # 业务前端（5173）
.\start-backend.ps1      # 参考后端（8080，需先停若依后端或改端口）
```

MySQL 需常驻（本机为 `MySQL84` 服务）。若依库 `ry-vue` 与业务库 `grade_manage` 的建库步骤见
[RUNBOOK-若依环境.md](RUNBOOK-若依环境.md)。

## 七、与若依相关的三处如实说明

1. **若依 ruoyi-generator 已真实运行**：本机安装了 Redis 8.10.3（`tools/redis`），RuoYi-Vue 3.9.2 后端跑在 8080，
   用 `POST /tool/gen/importTable` 导入 `biz_score`、`PUT /tool/gen` 配置包名与实体名、`GET /tool/gen/batchGenCode`
   下载代码，落位后编译通过，菜单 2000~2006 已插入 `sys_menu`。完整记录见 RUNBOOK 第五节。
2. **业务库与若依库是分开的**：若依自带 31 张框架表，其 `sys_user` 等与业务库的简化表同名但结构不同，
   因此新建 `ry-vue` 库导入框架表 + 9 张业务表（不含业务库的 `sys_user`）。若依登录仍用框架的 `admin/admin123`。
3. **`backend/` 是自研参考实现，不是若依工程**：它按文档第 6 章的接口契约实现 21 个接口（Spring Boot 3 + JdbcTemplate
   + 进程内令牌），用于验证「学生端—班主任端」的业务闭环；若依侧目前覆盖的是「成绩管理」标准 CRUD。
   两者的差异与替换点写在 `backend/README.md` 第 5 节。

## 七、文档如何修改

- 文档为**真实 Word 文件**，标题使用 Word 内置标题样式，改动后可在 Word 中「引用 → 更新目录」；
  当前目录是已排好真实页码的静态目录，若大幅增删内容，建议用生成脚本重跑。
- 工作区 `build/` 保留生成脚本：`doc2.py`（概要设计）、`doc3.py`（详细设计）、`figs*.py`（插图）、
  `docx_common.py`（排版与目录页码回填）。执行 `python doc3.py` 会先渲染 PDF 测页码，再回填目录并输出 docx。

## 八、数据与代码的一致性

| 文档内容 | 来源 |
|---|---|
| 8 个页面、12 个组件、2 个 store、3 个 util 的文件与行数 | `grade-front/src` 实际文件 |
| 校验规则（10~200 字、5~100 字、0~满分 等） | `src/utils/validate.js` 与后端 `ScoreService` / `AppealService` |
| 等级阈值、排名规则、年级排名正态估算参数 | `src/utils/score.js` 与后端 `MetaService.levelOf` / `biz_score_total` |
| 预测参数（gap=2、damping=0.5、recentCount=3）与置信度判据 | 前端 `PREDICT_CONFIG`、后端 `application.yml` 的 `grade.predict.*` |
| 六名学生六次月考总分、林晓逐科预测 661（634~688） | 前端 mock 算法与后端接口实测一致 |
| 表结构、索引、枚举 | `sql/grade_manage.sql` |
