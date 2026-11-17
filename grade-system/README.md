# 成绩管理系统

`backend/` 包含 RuoYi-Vue 后端和 Vue 2 管理端，`user-front/` 是 Vue 3 学生和教师门户。数据库脚本在 `db/`，验证与代码生成工具在 `tools/`。

## 环境准备

安装 JDK 17、Maven 3、Node.js 24 和 npm，启动 MySQL 和 Redis。默认连接 MySQL `localhost:3306`，用户 `root`，密码 `123456`，数据库 `ry-vue`。Redis 默认连接 `localhost:6379`，无密码。

连接信息有变动时，按需修改 [数据源配置](backend/ruoyi-admin/src/main/resources/application-druid.yml) 和 [Redis 配置](backend/ruoyi-admin/src/main/resources/application.yml)。

## 初始化数据库

以下命令从 `grade-system/` 运行，适用于 Linux、macOS 或 Git Bash。请在空的演示库中按顺序导入三个文件，使用 MySQL 图形客户端时顺序相同。业务初始化脚本会重建成绩管理业务表。

```bash
mysql -u root -p -e 'CREATE DATABASE IF NOT EXISTS `ry-vue` CHARACTER SET utf8mb4'
mysql -u root -p --default-character-set=utf8mb4 ry-vue < backend/sql/ry_20250522.sql
mysql -u root -p --default-character-set=utf8mb4 ry-vue < backend/sql/quartz.sql
mysql -u root -p --default-character-set=utf8mb4 ry-vue < db/00_grade_system_all.sql
```

## 启动服务

从 `grade-system/` 分别打开三个终端执行。首次启动时先安装两个前端的依赖。

后端

```bash
cd backend
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

管理端

```bash
cd backend/ruoyi-ui
npm install --legacy-peer-deps
NODE_OPTIONS=--openssl-legacy-provider npm run dev
```

学生和教师门户

```bash
cd user-front
npm ci
npm run dev
```

Windows 用户可以在完成数据库初始化和前端依赖安装后运行 `build-backend.bat`，再运行 `start-all.bat`。构建脚本使用环境中的 Maven，后端启动脚本优先使用 `JAVA_HOME` 中的 JDK。

| 入口 | 地址 | 演示账号 |
| --- | --- | --- |
| 若依管理端 | http://localhost:8081 | 管理员 `admin / admin123` |
| 学生和教师门户 | http://localhost:5173 | 学生 `2021001 / 123456`、教师 `T001 / 123456` |
| 后端 Swagger UI | http://localhost:8080/swagger-ui/index.html | 通过后端登录接口获取令牌 |

## AI 咨询助手

在启动后端前设置环境变量 `DEEPSEEK_API_KEY`。学生或教师登录门户后，点击右下角的 AI 按钮进入对话。缺少 Key 时，页面会显示配置提示，成绩和查分功能可以继续使用。

## 实验资料

- [实验一至实验四综合报告](report/成绩管理系统_软件工程实验报告.docx)
- [数据库设计](docs/02_数据库设计.md)与[接口设计](docs/03_接口设计.md)
- [若依代码生成操作手册](docs/04_生成器操作手册.md)
- [测试与验证](docs/05_测试与验证.md)
