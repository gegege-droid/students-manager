# 高中成绩管理系统 · 后端（Spring Boot 3 + MySQL）

本工程是《实验二 概要设计说明书》《实验三 详细设计说明书》中「服务端」部分的**参考实现**：
按文档第 6 章的接口契约、第 11 章的事务与并发约定，把 6 个模块的接口真正写出来并跑通。
它不改变前端（前端仍使用内置演示数据源，可离线运行），二者的数据完全一致。

## 一、技术栈

| 项 | 选择 | 说明 |
|---|---|---|
| JDK | 21（Temurin） | 本机已装 |
| 框架 | Spring Boot 3.3.5 | Web + JDBC + Validation |
| 持久层 | JdbcTemplate | 文档中写的是 MyBatis-Plus；此处为减少依赖改用 JdbcTemplate，SQL 与建表脚本一致 |
| 数据库 | MySQL 8.4（本机 `MySQL84` 服务） | 库名 `grade_manage` |
| 接口文档 | springdoc-openapi 2.6.0 | `/swagger-ui.html` |
| 会话 | 进程内 Map + 30 分钟滑动过期 | 文档设计为 JWT + Redis；本机无 Redis，替换点为 `TokenService` |
| 密码 | BCrypt（`BCryptPasswordEncoder`） | 与文档一致 |

## 二、启动

```powershell
# 1) 建库建表（只做一次）
mysql -uroot -proot -e "source deliver/sql/grade_manage.sql"
mysql -uroot -proot -e "source deliver/sql/demo_data.sql"     # 可选：灌入完整演示数据

# 2) 打包并启动
cd deliver/backend
mvn -B -DskipTests package
java -Dfile.encoding=UTF-8 -jar target/grade-server-1.0.0.jar
```

或在交付目录直接执行 `..\start-backend.ps1`。

- 服务地址：<http://localhost:8080>
- 接口文档：<http://localhost:8080/swagger-ui.html>
- 健康检查：`curl http://localhost:8080/v3/api-docs`

数据库连接配置在 `src/main/resources/application.yml`（默认 `root/root`，库 `grade_manage`）。

## 三、接口清单（18 个，与《概要设计说明书》§6.2 对应）

| 分组 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 认证 | GET | `/captchaImage` | 演示验证码（明文返回，便于测试） |
| 认证 | POST | `/login` | 登录，返回 token |
| 认证 | GET | `/getInfo` | 当前用户 |
| 认证 | POST | `/logout` | 退出 |
| 基础 | GET | `/business/exam/list` | 月考场次 |
| 基础 | GET | `/business/subject/list` | 科目字典 |
| 基础 | GET | `/business/class/list` | 班级 |
| 基础 | GET | `/business/student/profile/{id}` | 学生档案（含选科） |
| 成绩 | GET | `/business/score/matrix` | 成绩单矩阵 |
| 成绩 | GET | `/business/score/list` | 成绩分页/条件查询 |
| 成绩 | GET | `/business/score/roster` | 录入名单 |
| 成绩 | POST | `/business/score/batch` | 批量录入（upsert） |
| 成绩 | PUT | `/business/score` | 修改并留痕 |
| 成绩 | GET | `/business/score/log/list` | 修改记录 |
| 成绩 | GET | `/business/score/total/list` | 总分与排名 |
| 查分 | GET | `/business/appeal/list` | 待受理/已处理列表 |
| 查分 | GET | `/business/appeal/mine` | 我的申请 |
| 查分 | GET | `/business/appeal/{id}` | 申请详情 |
| 查分 | POST | `/business/appeal` | 提交申请 |
| 查分 | PUT | `/business/appeal/handle` | 受理（维持/更正） |
| 查分 | PUT | `/business/appeal/reject` | 驳回 |
| 预测 | GET | `/business/predict/student/{id}` | 高考成绩预测 |
| 演示 | POST | `/demo/resetPassword` | 把演示账号密码重置为 123456 |

除 `/login`、`/captchaImage`、`/demo/**`、Swagger 相关路径外，其余接口都需要请求头
`Authorization: Bearer <token>`；未登录返回 `{"code":401}`。

## 四、已实测通过的场景

| # | 场景 | 结果 |
|---|---|---|
| 1 | 学生 20230101 / 123456 登录 | 返回 token、角色 STUDENT、姓名林晓 |
| 2 | 学生成绩单矩阵 | 语文 119/127/122/127/122/129；总分 642→649→644→668→665→670；班级第 1、年级第 15 |
| 3 | 高考成绩预测 | 总分 661（区间 634~688），置信度 HIGH；数学 131（125~137），与前端页面、文档 §9.8 完全一致 |
| 4 | 重复申请同一场次科目 | 被拦截：`[B4002] 该场次该科目已有待受理的查分申请` |
| 5 | 学生提交 E05 物理查分 | 生成 `AP20261031001`，快照原分 92 |
| 6 | 班主任受理并更正为 95 | 申请变 ACCEPTED/CORRECT，成绩 92→95、状态 MODIFIED，写入 `source=APPEAL` 的修改记录 |
| 7 | 班主任批量录入 E07 数学 6 人 | `saved=6`，名单全部显示已录 |
| 8 | 越界分数（151 分） | 整批回滚，`[B3001] 分数 151 超出 0～150 的范围` |
| 9 | 修改成绩 133→130（填原因） | 成功，生成 `LOG...` 记录，重算 E07 总分排名 |
| 10 | 学生越权调用批量录入 | 被拦截：`[B2002] 只有班主任可以执行该操作` |
| 11 | 无 token 访问业务接口 | 返回 `code=401` |

## 五、与设计文档的差异（有意为之）

1. **持久层**：文档写 MyBatis-Plus，实现用 JdbcTemplate（少一层 XML 与依赖）。SQL 语义、唯一键 upsert、事务边界与文档 §11 一致。
2. **会话**：文档写 JWT + Redis，实现改为进程内 Map（本机无 Redis）。替换点集中在 `TokenService`，接口签名不变。
3. **分页**：文档约定 `pageNum/pageSize`，实现为「一次返回 + total」，数据量小（216 条）无需真分页。
4. 演示用 `/demo/resetPassword`、`/captchaImage` 明文验证码，仅用于课堂演示，生产必须移除。
