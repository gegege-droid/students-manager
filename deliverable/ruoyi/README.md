# 若依（RuoYi-Vue 3.9.2）集成产物

本目录存放「把业务表接入若依管理端」时**由 ruoyi-generator 生成的代码**，以及为跑通若依所做的配置改动。
完整的运行步骤、实测记录与排错见上级目录的 [`RUNBOOK-若依环境.md`](../RUNBOOK-若依环境.md)。

## 一、生成过程（真实执行，非示意）

环境：RuoYi-Vue 3.9.2（`springboot3` 分支，Spring Boot 3.5.16，JDK 21）+ MySQL 8.4 + Redis 8.10.3。

```text
1) 导入表   POST /tool/gen/importTable   tables=biz_score&tplWebType=element-plus
2) 改配置   PUT  /tool/gen               包名 com.ruoyi.business、模块 business、实体 Score、功能名「月考成绩」
3) 下载代码 GET  /tool/gen/batchGenCode   tables=biz_score    → 9.9 KB zip
4) 落位     Java/XML → ruoyi-admin 模块；Vue → ruoyi-ui；执行 score_menu.sql
5) 重新打包并重启，管理端出现菜单「成绩管理 → 月考成绩」
```

## 二、目录结构

```
ruoyi/
├── generated/
│   ├── java/com/ruoyi/business/       生成后放入 ruoyi-admin/src/main/java/com/ruoyi/business
│   │   ├── controller/ScoreController.java
│   │   ├── domain/Score.java
│   │   ├── mapper/ScoreMapper.java
│   │   └── service/IScoreService.java、service/impl/ScoreServiceImpl.java
│   ├── mapper/business/ScoreMapper.xml  放入 ruoyi-admin/src/main/resources/mapper/business
│   └── vue/
│       ├── api/score.js               放入 ruoyi-ui/src/api/business/score.js
│       └── views/index.vue            放入 ruoyi-ui/src/views/business/score/index.vue
├── config/application-druid.yml       改过数据源的配置文件（见下）
└── score_menu.sql                     菜单与按钮权限（实测 menu_id 2000~2006）
```

## 三、对若依的配置改动（仅 2 处）

| 文件 | 改动 | 原因 |
|---|---|---|
| `ruoyi-admin/src/main/resources/application-druid.yml` | 主库 URL 指向 `ry-vue`；`useSSL=false&allowPublicKeyRetrieval=true`；密码改为 `root` | MySQL 8.4 默认要求 SSL，且本机 root 密码为 root |
| 数据库 | 新建 `ry-vue` 库：导入 `sql/ry_20260417.sql` + `sql/quartz.sql`（31 张框架表），再导入 9 张业务表（`sys_class`、`sys_subject`、`biz_student`、`biz_exam`、`biz_score`、`biz_score_total`、`biz_appeal`、`biz_score_log`、`biz_predict`） | 若依自带 `sys_user`/`sys_menu` 等表，与业务库的简化表同名但结构不同，因此框架库与业务库分开，只把不冲突的业务表导入 |

Redis 保持默认（`spring.data.redis.host=localhost`、`port=6379`、无密码），无需改动。

## 四、生成页面的两处手工微调

生成器产出的列表页字段宽度按内容自适应，长字段（成绩ID）会折行，因此做了两处非功能性调整：

1. 表格列加 `width` 与 `show-overflow-tooltip`；
2. 表单项标签「得分率 = score / full_score」「月考场次编号」「科目编号」改为「得分率」「月考场次」「科目」。

业务逻辑、接口调用与权限注解均保持生成时的原样。

## 五、接口实测

| 操作 | 请求 | 结果 |
|---|---|---|
| 列表 | `GET /business/score/list?pageNum=1&pageSize=3` | `total=222`，返回 3 行 |
| 条件查询 | `GET /business/score/list?studentId=20230101&examId=E06` | 6 门科目，含得分率与等级 |
| 新增 | `POST /business/score` | 200 |
| 修改 | `PUT /business/score` | 200 |
| 删除 | `DELETE /business/score/{scoreId}` | 200 |
| 菜单下发 | `GET /getRouters` | 顶层包含 `/business` |
