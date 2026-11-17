# 实验四 · Swagger 接口测试与测试用例

本目录是实验四的非报告类成果：Swagger 接口测试截图 + 测试用例设计与执行记录。

## 内容

```
exp4/
├─ 测试用例执行记录.md     27 条用例（14 有效性 + 13 无效性），含输入/预期/实际/结论
├─ 测试用例执行记录.json   原始执行数据，便于复现与引用
└─ 截图/
   ├─ 01~07 ...             Swagger 操作过程（TestController、学生管理）
   └─ 用例xx-...-成功/失败.png  12 条代表性用例的执行结果截图
```

## 测试环境

| 项 | 版本 |
|---|---|
| JDK | Corretto 17 |
| Spring Boot | 2.5.15 |
| MySQL | 5.7.26 |
| 后端 | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui/index.html |

## 如何复现

1. 启动 MySQL、Redis，导入 `grade-system/db/00_grade_system_all.sql`；
2. 启动后端（JDK 17）；
3. 浏览器打开 Swagger，点 **Authorize**；
4. 获取 token：

```powershell
(Invoke-RestMethod -Uri http://localhost:8080/login -Method Post -ContentType 'application/json' -Body '{"username":"admin","password":"admin123"}').token
```

5. 在 Authorize 弹窗里粘贴 `Bearer <token>`；
6. 执行 `/system/student/*` 与 `/test/user/*` 接口，对照 `测试用例执行记录.md` 核对结果。

## 关于 AI 咨询功能的 API Key

`grade-system` 中的 AI 咨询助手需要 **你自己的 DeepSeek API Key**。

- 本仓库**不包含任何 API Key**，请勿把他人的 Key 提交上来；
- 配置方式见 `grade-system/README.md` 的「AI 咨询助手」一节：在本地新建 `backend/config/application.yml`（已被 `.gitignore` 排除）或设置环境变量 `DEEPSEEK_API_KEY`；
- 不配置 Key 时，AI 对话栏会提示「尚未配置 API Key」，其余功能不受影响。

## 测试账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 学生 | 2021001 | 123456 |
| 教师 | T001 | 123456 |
