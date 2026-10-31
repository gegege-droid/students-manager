# 若依（RuoYi-Vue 3.9.2）+ Redis 环境说明与运行手册

本文件记录「把若依后端 + Redis + 若依前端（Vue3）统一跑起来」的完整过程和结果，
以及用 **ruoyi-generator 真实生成月考成绩管理端代码** 的步骤与产物。

## 一、当前实际运行的服务（2026-10-31 实测）

| 服务 | 版本 | 地址 | 说明 |
|---|---|---|---|
| MySQL | 8.4.9（本机服务 `MySQL84`） | `127.0.0.1:3306`，root/root | 业务库 `grade_manage`、若依库 `ry-vue` |
| Redis | 8.10.3（Windows 版，见 `tools/redis`） | `127.0.0.1:6379` | 无密码；若依的验证码/令牌/字典缓存都落在这里（`DBSIZE` 有数据） |
| 若依后端 | RuoYi-Vue **3.9.2**（springboot3 分支，Spring Boot 3.5.16，JDK 21） | <http://localhost:8080> | 含 RuoYi 全部模块 + 生成的成绩管理接口 |
| 若依前端 | RuoYi-Vue3（Vue 3 + Vite + Element Plus） | <http://localhost:1024> | 开发服务器，`/dev-api` 反向代理到 8080 |
| 业务前端（可选） | grade-front（Vue 3 + Vite） | <http://localhost:5173> | 学生/班主任端 8 个页面，数据来自内置演示数据 |
| 后端参考实现（可选） | Spring Boot 3.3.5 | <http://localhost:8081>（默认 8080，与若依冲突时改端口） | 见 `backend/`，与 `grade_manage` 库配套 |

**登录账号**

| 系统 | 账号 | 密码 |
|---|---|---|
| 若依管理端（1024） | `admin` | `admin123` |
| 业务前端（5173） | 学生 `20230101` / 班主任 `T001` | `123456` |

## 二、目录结构

```
deliver/
├── ruoyi/
│   ├── backend/          RuoYi-Vue 3.9.2 后端源码（springboot3 分支）
│   │   ├── sql/          ry_20260417.sql（框架表）、quartz.sql
│   │   ├── ruoyi-admin/  Web 模块；生成的成绩管理代码在 src/main/java/com/ruoyi/business
│   │   └── ...           common / framework / system / generator / quartz
│   └── ui/               RuoYi-Vue3 前端源码；生成的页面在 src/views/business/score
├── tools/redis/          Redis 8.10.3（Windows x64），redis-server.exe + redis.conf
├── backend/              自研 Spring Boot 后端（参考实现，接口与文档一一对应）
├── grade-front/          业务前端（学生/班主任端）
└── sql/                  grade_manage 建表脚本 + 完整演示数据
```

## 三、从零启动（一条命令）

```powershell
.\start-ruoyi.ps1
```

脚本会依次：启动 Redis → 检查/构建若依后端 jar → 启动后端（8080）→ 安装/启动若依前端（1024）。

手动步骤（等价）：

```powershell
# 1) Redis
cd deliver\tools\redis\Redis-8.10.3-Windows-x64-msys2
.\redis-server.exe redis.conf

# 2) 数据库（仅首次）：建 ry-vue 库并导入若依脚本 + 业务表
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4;"
mysql -uroot -proot ry-vue -e "source deliver/ruoyi/backend/sql/ry_20260417.sql"
mysql -uroot -proot ry-vue -e "source deliver/ruoyi/backend/sql/quartz.sql"
mysql -uroot -proot ry-vue -e "source <业务表导出>.sql"     # sys_class/sys_subject/biz_* 共 9 张

# 3) 若依后端
cd deliver\ruoyi\backend
mvn -B -DskipTests package
java -Dfile.encoding=UTF-8 -jar ruoyi-admin\target\ruoyi-admin.jar

# 4) 若依前端（默认 80 端口，这里用 1024 避免占端口需管理员）
cd deliver\ruoyi\ui
npm install
npm run dev -- --port 1024
```

## 四、为跑通若依做的配置改动（共 2 处）

| 文件 | 改动 | 原因 |
|---|---|---|
| `ruoyi-admin/src/main/resources/application-druid.yml` | 主库 URL 指向 `ry-vue`，`useSSL=false&allowPublicKeyRetrieval=true`，密码改为 `root` | MySQL 8.4 默认要求 SSL 且本地 root 密码为 root |
| 数据库 | 新建 `ry-vue` 库，导入若依框架 31 张表 + 业务表 9 张（`sys_class`、`sys_subject`、`biz_student/exam/score/score_total/appeal/score_log/predict`） | 若依自带 `sys_user` 等表与业务表的 `sys_user` 同名，故业务库独立，只把不冲突的业务表导入若依库 |

Redis 配置保持默认（`spring.data.redis.host=localhost:6379`，无密码），无需改动。

> 为什么没有直接把 `grade_manage` 当若依库：若依有 31 张框架表，且 `sys_user`/`sys_menu` 等表结构与业务库的简化版 `sys_user` 不同。
> 做法是把业务表（除 `sys_user` 外）导入 `ry-vue`，若依的登录仍用框架自己的 `sys_user`（admin）。

## 五、用 ruoyi-generator 生成「月考成绩」管理端（实测记录）

这一步是课程要求「设计数据库表并用 ruoyi-generator 生成代码完成管理端开发」，本次**真实执行**了，过程如下：

1. 导入表：`POST /tool/gen/importTable`，参数 `tables=biz_score&tplWebType=element-plus`（3.9.x 新增的前端模板类型参数）。
2. 编辑生成配置：`PUT /tool/gen`，设置
   - 包名 `com.ruoyi.business`、模块名 `business`、实体类 `Score`、业务名 `score`、功能名「月考成绩」。
3. 生成并下载：`GET /tool/gen/batchGenCode?tables=biz_score`，得到 9.9 KB 的 zip，内含 8 个文件：

```
scoreMenu.sql                                  菜单与按钮权限 SQL
main/java/com/ruoyi/business/controller/ScoreController.java
main/java/com/ruoyi/business/domain/Score.java
main/java/com/ruoyi/business/mapper/ScoreMapper.java
main/java/com/ruoyi/business/service/IScoreService.java
main/java/com/ruoyi/business/service/impl/ScoreServiceImpl.java
main/resources/mapper/business/ScoreMapper.xml
vue/api/business/score.js
vue/views/business/score/index.vue
```

4. 落位：Java 与 XML 放入 `ruoyi-admin` 模块（`com.ruoyi.business` 包），Vue 放入 `ruoyi-ui3/src/api|views/business/score`。
5. 菜单：执行 `scoreMenu.sql`（在其前面补一个一级目录「成绩管理」，父级 0），生成菜单 2000~2006。
6. 重新打包并重启后端，前端热更新即可看到菜单：**成绩管理 → 月考成绩**。

**实测接口**（登录后带 token）：

| 操作 | 请求 | 结果 |
|---|---|---|
| 列表 | `GET /business/score/list?pageNum=1&pageSize=3` | `total=222`，返回 3 行 |
| 条件查询 | `GET /business/score/list?studentId=20230101&E06` | 6 门科目，得分率/等级齐全 |
| 新增 | `POST /business/score`（E07 语文 126） | 成功 |
| 修改 | `PUT /business/score`（改为 131，等级 A） | 成功 |
| 删除 | `DELETE /business/score/{scoreId}` | 成功 |
| 菜单下发 | `GET /getRouters` | 顶层路由包含 `/business` |

生成页面已做两处微调（宽度与表单项标签），避免长字段折行难读。

## 六、与设计文档的对应关系

| 设计文档 | 若依环境中的落点 |
|---|---|
| 第 5 章 10 张表 | `sql/grade_manage.sql` 建表；其中 9 张业务表已导入 `ry-vue`，可直接被代码生成器识别 |
| 第 6 章 24 个接口 | 若依侧目前落地了「成绩管理」的 6 个标准 CRUD 接口（生成器产出）；其余业务接口见 `backend/` 参考实现 |
| 9.3 节「代码生成器使用方式」 | 本文第五节即该步骤的真实执行记录 |
| 第 11 章 事务与并发 | 参考实现 `backend/` 中按文档实现；若依生成的是单表 CRUD，事务由框架默认处理 |

## 七、常见问题

| 现象 | 原因 | 处理 |
|---|---|---|
| 登录提示「验证码错误」 | Redis 没起来，验证码无法校验 | 先启动 Redis，确认 `redis-cli ping` 返回 PONG |
| 后端启动报 `Access denied for user 'root'` | 数据源密码不是 root | 改 `application-druid.yml` 的 `password` |
| 前端 1024 打不开 | 端口被占或 npm 未装 | `Get-NetTCPConnection -LocalPort 1024`；`npm install` |
| 菜单里看不到「成绩管理」 | 未执行 `scoreMenu.sql` 或未重新登录 | 执行 SQL；退出重新登录（菜单有 Redis 缓存，必要时 `FLUSHDB`） |
| Vite 报 `EBUSY ... tmpdir` | Windows 下 Vite 监听临时文件（本机编辑文件时触发过） | 重新 `npm run dev` 即可 |
