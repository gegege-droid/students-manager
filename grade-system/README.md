# 实验三 · 成绩管理系统（RuoYi-Vue + 代码生成 + 用户侧前后端）

本目录是实验三的独立交付工程，基于实验二的 RuoYi-Vue v3.9.1：

- **管理端**：设计成绩管理数据库表，运行 `ruoyi-generator` 生成 6 个 CRUD 模块；
- **用户侧（加分项）**：复用实验一的 Vue3 成绩管理前端，接入 RuoYi 后端，实现学生查成绩/申请查分、教师录成绩/改成绩/受理查分的完整闭环；另新增**个人中心**、**预测下次考试成绩**、**最近成绩趋势折线图**与 **AI 咨询助手（DeepSeek）**；
- **文档**：按《网上招聘系统详细设计》样例撰写详细设计报告（≥15 页）。

## 一、目录结构

```
grade-system/
├─ backend/                 RuoYi-Vue 3.9.1（已集成生成代码与 Portal API）
├─ user-front/              用户侧 Vue3 前端（实验一 grade-front 改造版）
├─ db/                      数据库脚本
│  ├─ 00_grade_system_all.sql   一键初始化（推荐）
│  ├─ 01_schema_grade.sql       7 张 edu_* 表
│  ├─ 02_seed_grade.sql         演示种子数据
│  ├─ 03_portal_accounts.sql    门户账号 + 关闭验证码
│  ├─ 04_menu_grade.sql         管理端「成绩管理」菜单
│  └─ 05_checks.sql             导入后自检
├─ generated/               ruoyi-generator 下载的 zip 与解压内容
├─ docs/                    数据库、接口、生成器、测试说明
├─ tools/                   生成器自动化、API/UI 冒烟测试、报告生成脚本
├─ report/                  详细设计报告 docx/pdf 与图表/截图
└─ README.md
```

## 二、环境与端口

| 组件 | 说明 |
|---|---|
| JDK | **JDK 17**（`C:\Users\lenovo\.jdks\corretto-17.0.19`），不要用 JDK 25 |
| Maven | IDEA 内置 Maven 或本地 Maven 3.x |
| MySQL | phpStudy MySQL 5.7.26，`localhost:3306`，root / **123456**，数据库 `ry-vue` |
| Redis | `localhost:6379`，无密码 |
| 后端 | http://localhost:8080 |
| 管理端前端 | http://localhost:8081 （admin / admin123） |
| 用户侧前端 | http://localhost:5173 |

门户账号（密码均为 `123456`）：

| 角色 | 账号 | 姓名 |
|---|---|---|
| 学生 | 2021001 | 张三 |
| 学生 | 2021003 | 王五 |
| 学生 | 2021005 | 钱七 |
| 教师 | T001 | 李老师 |

> 门户账号复用 RuoYi `sys_user`，通过 `db/03_portal_accounts.sql` 写入；登录验证码已在 `sys_config` 中关闭。

### AI 咨询助手（可选）

> **重要：本仓库不包含任何 API Key。** 请使用你自己的 DeepSeek API Key，按下面步骤在本地配置；不要把他人的 Key 提交到仓库。

用户端右下角蓝色按钮 → 右侧滑出对话栏，由 DeepSeek 提供回答。默认关闭，需要配置 API Key：

1. 在 https://platform.deepseek.com/ 创建 Key；
2. 新建 `backend/config/application.yml`（该目录已被 `.gitignore` 排除）：

```yaml
deepseek:
  api-key: sk-你的key
  model: deepseek-chat
```

或启动前设置环境变量 `DEEPSEEK_API_KEY`；
3. 重启后端，打开 `http://localhost:5173`，学生/教师登录后点右下角按钮。

> Key 只保存在服务端，前端通过 `/portal/ai/chat` 代理调用；未配置时对话栏会提示「尚未配置 API Key」。

## 三、快速启动

### 快捷启动（推荐）

确保 MySQL 3306、Redis 6379 已运行，然后任选一种：

1. **双击** `start-all.bat`：自动打开三个窗口，依次启动后端、管理端、用户端；
2. **VS Code**：打开本目录，按 `Ctrl + Shift + B`，或 `终端 → 运行任务 → 一键启动全部`；
3. 只想启动某一个：双击 `start-backend.bat` / `start-admin-ui.bat` / `start-user-front.bat`；
4. 停止：双击 `stop-all.bat`（只停 8080/8081/5173，不动 MySQL/Redis）。

改过后端代码时，先双击 `build-backend.bat` 重新打包，再启动后端。

### 1. 导入数据库

在 Navicat / phpMyAdmin 中打开 `ry-vue`，执行：

```
db/00_grade_system_all.sql
```

或命令行：

```cmd
D:\IDEA\phpStudy_64\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysql.exe ^
  -uroot -p123456 --default-character-set=utf8mb4 ry-vue < db\00_grade_system_all.sql
```

### 2. 启动后端

```cmd
cd backend
set JAVA_HOME=C:\Users\lenovo\.jdks\corretto-17.0.19
mvn clean package -DskipTests
%JAVA_HOME%\bin\java.exe -jar ruoyi-admin\target\ruoyi-admin.jar
```

看到 `Started RuoYiApplication` 即成功。Maven 镜像已指向阿里云；若依赖未下载完，重新执行即可。

### 3. 启动管理端

```cmd
cd backend\ruoyi-ui
set NODE_OPTIONS=--openssl-legacy-provider
npm run dev
```

访问 http://localhost:8081 ，使用 `admin / admin123` 登录；左侧「成绩管理」下包含成绩、查分申请、课程、学生、教师、班级 6 个生成模块。

### 4. 启动用户侧

```cmd
cd user-front
npm run dev
```

访问 http://localhost:5173 ，学生 `2021001 / 123456`，教师 `T001 / 123456`。

## 四、ruoyi-generator 操作要点

生成器配置：`backend/ruoyi-generator/src/main/resources/generator.yml`

```yaml
gen:
  author: grade-team
  packageName: com.ruoyi.system
  autoRemovePre: true     # 去掉 edu_ 前缀
  tablePrefix: edu_
```

流程（详细步骤见 `docs/04_生成器操作手册.md`）：

1. 启动后端，admin 登录；
2. 「系统工具 → 代码生成 → 导入」选择 7 张 `edu_*` 表；
3. 编辑：`tplCategory=crud`，`parentMenuId=2000`，包路径 `com.ruoyi.system`；
4. 预览 / 下载 `ruoyi.zip`；
5. 将 Java/XML 复制到 `ruoyi-system`，Vue2 页面复制到 `ruoyi-ui`；
6. 执行 `db/04_menu_grade.sql` 生成菜单；
7. 重启后端，刷新管理端。

`tools/gen_admin.py` 已把上述步骤自动化，可重复执行。

## 五、验证

```cmd
:: API 冒烟测试（会写入测试数据）
python tools\smoke_test.py

:: 用户侧页面冒烟（需要 Edge 以 --remote-debugging-port=9222 启动）
node tools\ui_smoke.mjs

:: 管理端页面冒烟
node tools\admin_ui_smoke.mjs
```

测试完成后可重新执行 `db/02_seed_grade.sql` 恢复初始演示数据。

## 六、详细设计报告

- `report/实验三_详细设计报告.docx`
- `report/实验三_详细设计报告.pdf`
- 图表与页面截图：`report/assets/`

报告包含：封面、目录、系统设计概述、数据库 ER 图、接口约定、成绩状态机、查分状态机、5 个模块的详细设计（文件结构、调用关系、伪代码、流程图、校验规则）、代码生成模块、复用模块、配置文件、附录 DDL/接口/测试用例。

封面项目组成员暂为占位行，拿到名单后在 Word 中补填即可。

## 七、注意事项

- 系统默认 JDK 25 与 Spring Boot 2.5.15 不兼容，构建/运行必须用 JDK 17；
- MySQL 5.7 未开启 SSL，JDBC URL 已设置 `useSSL=false`；
- 管理端 Vue2 + webpack4 在 Node 24 下需要 `NODE_OPTIONS=--openssl-legacy-provider`；
- 用户端 Vite8 要求 Node ≥ 20.19，当前 Node 24 满足；
- 不要把实验二 `exp2/RuoYi-Vue` 与本目录混用，本目录是实验三的独立副本。
