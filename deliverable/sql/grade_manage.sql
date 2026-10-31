-- ============================================================================
--  高中成绩管理系统（新高考 3+1+2）  数据库建表脚本
--  适用：MySQL 8.0 / InnoDB / utf8mb4
--  命名与字段风格对齐 RuoYi-Vue（若依）代码生成器约定：
--    · 主键、审计字段（create_by / create_time / update_by / update_time / remark）
--    · 逻辑删除 del_flag（0 正常 / 2 删除）
--    · 表注释与字段注释齐全，便于 ruoyi-generator 直接生成管理端代码
--  说明：为避免导入顺序与级联删除麻烦，脚本不建外键约束，实体关系由应用层保证
--        （与若依框架的默认实践一致），关系说明见《概要设计说明书》第 5 章。
-- ============================================================================

DROP DATABASE IF EXISTS grade_manage;
CREATE DATABASE grade_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE grade_manage;

-- ----------------------------------------------------------------------------
-- 1. 班级
-- ----------------------------------------------------------------------------
CREATE TABLE sys_class (
  class_id            VARCHAR(20)  NOT NULL                COMMENT '班级编号（主键）',
  class_name          VARCHAR(50)  NOT NULL                COMMENT '班级名称，如 高三(2)班',
  grade_name          VARCHAR(50)  DEFAULT NULL            COMMENT '所属年级，如 2024级高三',
  grade_student_count INT          DEFAULT 0               COMMENT '年级总人数（用于年级排名估算）',
  head_teacher_id     VARCHAR(20)  DEFAULT NULL            COMMENT '班主任工号',
  sort_no             INT          DEFAULT 0               COMMENT '显示顺序',
  status              CHAR(1)      DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag            CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by           VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time         DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by           VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time         DATETIME     DEFAULT NULL            COMMENT '更新时间',
  remark              VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (class_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- ----------------------------------------------------------------------------
-- 2. 用户账号（学生 / 班主任统一登录主体）
-- ----------------------------------------------------------------------------
CREATE TABLE sys_user (
  user_id     BIGINT       NOT NULL AUTO_INCREMENT         COMMENT '用户ID（主键）',
  login_id    VARCHAR(20)  NOT NULL                        COMMENT '登录账号：学号 / 工号',
  user_name   VARCHAR(30)  NOT NULL                        COMMENT '姓名',
  password    VARCHAR(100) NOT NULL                        COMMENT '密码（BCrypt 密文）',
  user_type   VARCHAR(20)  NOT NULL                        COMMENT '用户类型（STUDENT 学生 / TEACHER 班主任）',
  class_id    VARCHAR(20)  DEFAULT NULL                    COMMENT '所属班级（学生）',
  title       VARCHAR(30)  DEFAULT NULL                    COMMENT '职称（教师）',
  phone       VARCHAR(11)  DEFAULT NULL                    COMMENT '手机号',
  status      CHAR(1)      DEFAULT '0'                     COMMENT '账号状态（0正常 1停用）',
  login_ip    VARCHAR(50)  DEFAULT ''                      COMMENT '最后登录IP',
  login_date  DATETIME     DEFAULT NULL                    COMMENT '最后登录时间',
  del_flag    CHAR(1)      DEFAULT '0'                     COMMENT '删除标志（0存在 2删除）',
  create_by   VARCHAR(64)  DEFAULT ''                      COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL                    COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT ''                      COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL                    COMMENT '更新时间',
  remark      VARCHAR(255) DEFAULT NULL                    COMMENT '备注',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_login (login_id),
  KEY idx_user_class (class_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

-- ----------------------------------------------------------------------------
-- 3. 科目字典（新高考 3+1+2）
-- ----------------------------------------------------------------------------
CREATE TABLE sys_subject (
  subject_id       VARCHAR(20) NOT NULL                    COMMENT '科目编号（主键），如 MATH',
  subject_name     VARCHAR(30) NOT NULL                    COMMENT '科目名称，如 数学',
  full_score       INT         NOT NULL                    COMMENT '该科高考满分（语文/数学/外语 150，其余 100）',
  subject_category VARCHAR(20) NOT NULL                    COMMENT '科目类别（REQUIRED 必考 / PRIMARY 首选 / SECONDARY 再选）',
  sort_no          INT         DEFAULT 0                   COMMENT '显示顺序',
  status           CHAR(1)     DEFAULT '0'                 COMMENT '状态（0正常 1停用）',
  del_flag         CHAR(1)     DEFAULT '0'                 COMMENT '删除标志（0存在 2删除）',
  create_by        VARCHAR(64) DEFAULT ''                  COMMENT '创建者',
  create_time      DATETIME    DEFAULT NULL                COMMENT '创建时间',
  update_by        VARCHAR(64) DEFAULT ''                  COMMENT '更新者',
  update_time      DATETIME    DEFAULT NULL                COMMENT '更新时间',
  remark           VARCHAR(255) DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目字典表';

-- ----------------------------------------------------------------------------
-- 4. 学生档案（选科信息是「成绩录入名单」的唯一依据）
-- ----------------------------------------------------------------------------
CREATE TABLE biz_student (
  student_id         VARCHAR(20) NOT NULL                  COMMENT '学号（主键）',
  user_id            BIGINT      DEFAULT NULL              COMMENT '绑定登录账号 user_id',
  student_name       VARCHAR(30) NOT NULL                  COMMENT '姓名',
  gender             CHAR(1)     DEFAULT NULL              COMMENT '性别（M 男 / F 女）',
  class_id           VARCHAR(20) NOT NULL                  COMMENT '所属班级编号',
  combination        VARCHAR(20) DEFAULT NULL              COMMENT '选科组合，如 物化生',
  primary_subject    VARCHAR(20) DEFAULT NULL              COMMENT '首选科目（PHYSICS / HISTORY）',
  secondary_subjects VARCHAR(50) DEFAULT NULL              COMMENT '再选科目（逗号分隔，如 CHEMISTRY,BIOLOGY）',
  status             CHAR(1)     DEFAULT '0'               COMMENT '状态（0在读 1离校）',
  del_flag           CHAR(1)     DEFAULT '0'               COMMENT '删除标志（0存在 2删除）',
  create_by          VARCHAR(64) DEFAULT ''                COMMENT '创建者',
  create_time        DATETIME    DEFAULT NULL              COMMENT '创建时间',
  update_by          VARCHAR(64) DEFAULT ''                COMMENT '更新者',
  update_time        DATETIME    DEFAULT NULL              COMMENT '更新时间',
  remark             VARCHAR(255) DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (student_id),
  UNIQUE KEY uk_student_user (user_id),
  KEY idx_student_class (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

-- ----------------------------------------------------------------------------
-- 5. 月考场次
-- ----------------------------------------------------------------------------
CREATE TABLE biz_exam (
  exam_id      VARCHAR(20) NOT NULL                        COMMENT '场次编号（主键），如 E06',
  exam_name    VARCHAR(50) NOT NULL                        COMMENT '场次名称，如 第六次月考',
  exam_date    DATE        DEFAULT NULL                    COMMENT '考试日期',
  stage        VARCHAR(30) DEFAULT NULL                    COMMENT '所属学期，如 高三下学期',
  score_status VARCHAR(20) DEFAULT 'NOT_ENTERED'           COMMENT '成绩状态（NOT_ENTERED 未录入 / ENTERING 录入中 / PUBLISHED 已发布）',
  sort_no      INT         DEFAULT 0                       COMMENT '显示顺序（同时作为场次先后的唯一依据）',
  del_flag     CHAR(1)     DEFAULT '0'                     COMMENT '删除标志（0存在 2删除）',
  create_by    VARCHAR(64) DEFAULT ''                      COMMENT '创建者',
  create_time  DATETIME    DEFAULT NULL                    COMMENT '创建时间',
  update_by    VARCHAR(64) DEFAULT ''                      COMMENT '更新者',
  update_time  DATETIME    DEFAULT NULL                    COMMENT '更新时间',
  remark       VARCHAR(255) DEFAULT NULL                   COMMENT '备注',
  PRIMARY KEY (exam_id),
  KEY idx_exam_sort (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月考场次表';

-- ----------------------------------------------------------------------------
-- 6. 成绩表（核心业务表，若依代码生成器的主表）
-- ----------------------------------------------------------------------------
CREATE TABLE biz_score (
  score_id    VARCHAR(64) NOT NULL                         COMMENT '成绩ID（主键）',
  student_id  VARCHAR(20) NOT NULL                         COMMENT '学号',
  exam_id     VARCHAR(20) NOT NULL                         COMMENT '月考场次编号',
  subject_id  VARCHAR(20) NOT NULL                         COMMENT '科目编号',
  score       INT         NOT NULL                         COMMENT '得分（0 ~ 该科满分）',
  score_rate  DECIMAL(6,4) DEFAULT NULL                    COMMENT '得分率 = score / full_score（冗余，便于排序统计）',
  level       VARCHAR(10) DEFAULT NULL                     COMMENT '等级（A 优秀 / B 良好 / C 及格 / D 待提高）',
  status      VARCHAR(20) DEFAULT 'NORMAL'                 COMMENT '状态（NORMAL 正常 / APPEALING 查分中 / MODIFIED 已更正）',
  recorder_id VARCHAR(20) DEFAULT NULL                     COMMENT '录入人（工号）',
  record_time DATETIME    DEFAULT NULL                     COMMENT '录入时间',
  del_flag    CHAR(1)     DEFAULT '0'                      COMMENT '删除标志（0存在 2删除）',
  create_by   VARCHAR(64) DEFAULT ''                       COMMENT '创建者',
  create_time DATETIME    DEFAULT NULL                     COMMENT '创建时间',
  update_by   VARCHAR(64) DEFAULT ''                       COMMENT '更新者',
  update_time DATETIME    DEFAULT NULL                     COMMENT '更新时间',
  remark      VARCHAR(255) DEFAULT NULL                    COMMENT '备注',
  PRIMARY KEY (score_id),
  UNIQUE KEY uk_score (student_id, exam_id, subject_id),
  KEY idx_score_exam_subject (exam_id, subject_id),
  KEY idx_score_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月考成绩表';

-- ----------------------------------------------------------------------------
-- 7. 月考核算结果（总分 / 班级排名 / 年级排名，供查询与预测复用）
-- ----------------------------------------------------------------------------
CREATE TABLE biz_score_total (
  total_id    BIGINT      NOT NULL AUTO_INCREMENT          COMMENT '主键',
  student_id  VARCHAR(20) NOT NULL                         COMMENT '学号',
  exam_id     VARCHAR(20) NOT NULL                         COMMENT '月考场次编号',
  total_score INT         DEFAULT NULL                     COMMENT '总分（各科之和）',
  score_rate  DECIMAL(6,4) DEFAULT NULL                    COMMENT '总分得分率 = total_score / 750',
  class_rank  INT         DEFAULT NULL                     COMMENT '班级排名（同分同名次）',
  grade_rank  INT         DEFAULT NULL                     COMMENT '年级排名（按正态分布估算）',
  calc_time   DATETIME    DEFAULT NULL                     COMMENT '计算时间',
  PRIMARY KEY (total_id),
  UNIQUE KEY uk_total (student_id, exam_id),
  KEY idx_total_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月考核算结果表';

-- ----------------------------------------------------------------------------
-- 8. 查分申请
-- ----------------------------------------------------------------------------
CREATE TABLE biz_appeal (
  appeal_id       VARCHAR(24) NOT NULL                     COMMENT '申请编号（主键）：AP + yyyyMMdd + 3 位流水',
  score_id        VARCHAR(64) DEFAULT NULL                 COMMENT '被申请的成绩记录ID',
  student_id      VARCHAR(20) NOT NULL                     COMMENT '申请学号',
  exam_id         VARCHAR(20) NOT NULL                     COMMENT '月考场次编号',
  subject_id      VARCHAR(20) NOT NULL                     COMMENT '科目编号',
  original_score  INT         DEFAULT NULL                 COMMENT '申请时的原分快照（不随成绩修改变化）',
  reason          VARCHAR(200) NOT NULL                    COMMENT '申请理由（10~200 字）',
  apply_time      DATETIME    NOT NULL                     COMMENT '申请时间',
  status          VARCHAR(20) DEFAULT 'PENDING'            COMMENT '状态（PENDING 待受理 / ACCEPTED 已受理 / REJECTED 已驳回）',
  handle_result   VARCHAR(20) DEFAULT NULL                 COMMENT '处理结果（KEEP 维持 / CORRECT 更正；驳回时为空）',
  corrected_score INT         DEFAULT NULL                 COMMENT '更正后的分数（仅 CORRECT 时填写）',
  opinion         VARCHAR(200) DEFAULT NULL                COMMENT '处理意见 / 驳回理由（5~200 字）',
  handler_id      VARCHAR(20) DEFAULT NULL                 COMMENT '受理人工号',
  handle_time     DATETIME    DEFAULT NULL                 COMMENT '受理时间',
  del_flag        CHAR(1)     DEFAULT '0'                  COMMENT '删除标志（0存在 2删除）',
  create_by       VARCHAR(64) DEFAULT ''                   COMMENT '创建者',
  create_time     DATETIME    DEFAULT NULL                 COMMENT '创建时间',
  update_by       VARCHAR(64) DEFAULT ''                   COMMENT '更新者',
  update_time     DATETIME    DEFAULT NULL                 COMMENT '更新时间',
  remark          VARCHAR(255) DEFAULT NULL                COMMENT '备注',
  PRIMARY KEY (appeal_id),
  KEY idx_appeal_status (status, apply_time),
  KEY idx_appeal_student (student_id),
  KEY idx_appeal_target (exam_id, subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查分申请表';

-- ----------------------------------------------------------------------------
-- 9. 成绩修改记录（留痕，不可修改、不可删除）
-- ----------------------------------------------------------------------------
CREATE TABLE biz_score_log (
  log_id        VARCHAR(32) NOT NULL                       COMMENT '记录ID（主键）',
  score_id      VARCHAR(64) NOT NULL                       COMMENT '成绩记录ID',
  student_id    VARCHAR(20) NOT NULL                       COMMENT '学号',
  exam_id       VARCHAR(20) NOT NULL                       COMMENT '月考场次编号',
  subject_id    VARCHAR(20) NOT NULL                       COMMENT '科目编号',
  old_score     INT         DEFAULT NULL                   COMMENT '修改前分数',
  new_score     INT         DEFAULT NULL                   COMMENT '修改后分数',
  change_reason VARCHAR(100) NOT NULL                      COMMENT '修改原因（5~100 字）',
  operator_id   VARCHAR(20) NOT NULL                       COMMENT '操作人工号',
  operate_time  DATETIME    NOT NULL                       COMMENT '操作时间',
  source        VARCHAR(20) NOT NULL DEFAULT 'MANUAL'      COMMENT '来源（MANUAL 手工修改 / APPEAL 查分更正）',
  appeal_id     VARCHAR(24) DEFAULT NULL                   COMMENT '来源查分申请ID（source=APPEAL 时填写）',
  PRIMARY KEY (log_id),
  KEY idx_log_score (score_id),
  KEY idx_log_student (student_id, exam_id, subject_id),
  KEY idx_log_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩修改记录表';

-- ----------------------------------------------------------------------------
-- 10. 高考成绩预测快照（可选：需要留存历史预测时启用）
-- ----------------------------------------------------------------------------
CREATE TABLE biz_predict (
  predict_id      BIGINT      NOT NULL AUTO_INCREMENT      COMMENT '主键',
  student_id      VARCHAR(20) NOT NULL                     COMMENT '学号',
  subject_id      VARCHAR(20) DEFAULT NULL                 COMMENT '科目编号（为空表示总分预测）',
  base_exam_id    VARCHAR(20) DEFAULT NULL                 COMMENT '计算基准场次（最后一次已出成绩的月考）',
  predicted_score INT         DEFAULT NULL                 COMMENT '预测分',
  low_score       INT         DEFAULT NULL                 COMMENT '预测区间下限',
  high_score      INT         DEFAULT NULL                 COMMENT '预测区间上限',
  confidence      VARCHAR(10) DEFAULT NULL                 COMMENT '置信度（HIGH / MEDIUM / LOW）',
  algorithm       VARCHAR(50) DEFAULT 'WMA-TREND-V1'       COMMENT '算法版本',
  predict_time    DATETIME    DEFAULT NULL                 COMMENT '预测时间',
  PRIMARY KEY (predict_id),
  KEY idx_predict_student (student_id, predict_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高考成绩预测快照表';

-- ============================================================================
--  演示数据（与前端 grade-front/src/api/mock.js 保持一致）
-- ============================================================================

INSERT INTO sys_class (class_id, class_name, grade_name, grade_student_count, head_teacher_id, sort_no, create_by, create_time) VALUES
('G3-2', '高三(2)班', '2024级高三', 480, 'T001', 1, 'admin', NOW());

INSERT INTO sys_subject (subject_id, subject_name, full_score, subject_category, sort_no, create_by, create_time) VALUES
('CHINESE',   '语文',     150, 'REQUIRED',  1, 'admin', NOW()),
('MATH',      '数学',     150, 'REQUIRED',  2, 'admin', NOW()),
('ENGLISH',   '外语',     150, 'REQUIRED',  3, 'admin', NOW()),
('PHYSICS',   '物理',     100, 'PRIMARY',   4, 'admin', NOW()),
('HISTORY',   '历史',     100, 'PRIMARY',   5, 'admin', NOW()),
('CHEMISTRY', '化学',     100, 'SECONDARY', 6, 'admin', NOW()),
('BIOLOGY',   '生物',     100, 'SECONDARY', 7, 'admin', NOW()),
('POLITICS',  '思想政治', 100, 'SECONDARY', 8, 'admin', NOW()),
('GEOGRAPHY', '地理',     100, 'SECONDARY', 9, 'admin', NOW());

INSERT INTO biz_exam (exam_id, exam_name, exam_date, stage, score_status, sort_no, create_by, create_time) VALUES
('E01', '第一次月考', '2026-09-05', '高三上学期', 'PUBLISHED',   1, 'admin', NOW()),
('E02', '第二次月考', '2026-10-10', '高三上学期', 'PUBLISHED',   2, 'admin', NOW()),
('E03', '第三次月考', '2026-11-07', '高三上学期', 'PUBLISHED',   3, 'admin', NOW()),
('E04', '第四次月考', '2026-12-05', '高三上学期', 'PUBLISHED',   4, 'admin', NOW()),
('E05', '第五次月考', '2027-03-13', '高三下学期', 'PUBLISHED',   5, 'admin', NOW()),
('E06', '第六次月考', '2027-04-10', '高三下学期', 'PUBLISHED',   6, 'admin', NOW()),
('E07', '第七次月考', '2027-05-08', '高三下学期', 'NOT_ENTERED', 7, 'admin', NOW());

-- 密码明文均为 123456，密文由后端 BCryptPasswordEncoder 生成（可直接登录）
INSERT INTO sys_user (login_id, user_name, password, user_type, class_id, title, create_by, create_time) VALUES
('T001',     '王老师', '$2a$10$V2UzrNoFXWbYbS7jmshUMe2RI58wuWHPhFizV3hzpVy22OnS7rE/C', 'TEACHER', NULL,   '高级教师', 'admin', NOW()),
('20230101', '林晓',   '$2a$10$V2UzrNoFXWbYbS7jmshUMe2RI58wuWHPhFizV3hzpVy22OnS7rE/C', 'STUDENT', 'G3-2', NULL,      'admin', NOW()),
('20230103', '苏雨桐', '$2a$10$V2UzrNoFXWbYbS7jmshUMe2RI58wuWHPhFizV3hzpVy22OnS7rE/C', 'STUDENT', 'G3-2', NULL,      'admin', NOW()),
('20230106', '顾清和', '$2a$10$V2UzrNoFXWbYbS7jmshUMe2RI58wuWHPhFizV3hzpVy22OnS7rE/C', 'STUDENT', 'G3-2', NULL,      'admin', NOW());

INSERT INTO biz_student (student_id, user_id, student_name, gender, class_id, combination, primary_subject, secondary_subjects, create_by, create_time) VALUES
('20230101', 2, '林晓',   'F', 'G3-2', '物化生', 'PHYSICS', 'CHEMISTRY,BIOLOGY',   'admin', NOW()),
('20230102', NULL, '陈宇航', 'M', 'G3-2', '物化生', 'PHYSICS', 'CHEMISTRY,BIOLOGY',   'admin', NOW()),
('20230103', 3, '苏雨桐', 'F', 'G3-2', '物化生', 'PHYSICS', 'CHEMISTRY,BIOLOGY',   'admin', NOW()),
('20230104', NULL, '周子墨', 'M', 'G3-2', '物生地', 'PHYSICS', 'BIOLOGY,GEOGRAPHY',   'admin', NOW()),
('20230105', NULL, '郑一诺', 'F', 'G3-2', '史政地', 'HISTORY', 'POLITICS,GEOGRAPHY',  'admin', NOW()),
('20230106', 4, '顾清和', 'M', 'G3-2', '史政地', 'HISTORY', 'POLITICS,GEOGRAPHY',  'admin', NOW());

-- 第六次月考：林晓（物化生）六科成绩示例
INSERT INTO biz_score (score_id, student_id, exam_id, subject_id, score, score_rate, level, status, recorder_id, record_time, create_by, create_time) VALUES
('SC20230101E06CHINESE', '20230101', 'E06', 'CHINESE',   129, 0.8600, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW()),
('SC20230101E06MATH',    '20230101', 'E06', 'MATH',      132, 0.8800, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW()),
('SC20230101E06ENGLISH', '20230101', 'E06', 'ENGLISH',   145, 0.9667, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW()),
('SC20230101E06PHYSICS', '20230101', 'E06', 'PHYSICS',    90, 0.9000, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW()),
('SC20230101E06CHEMISTRY','20230101','E06', 'CHEMISTRY',  89, 0.8900, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW()),
('SC20230101E06BIOLOGY', '20230101', 'E06', 'BIOLOGY',    85, 0.8500, 'A', 'NORMAL', 'T001', '2027-04-11 18:00:00', 'T001', NOW());

INSERT INTO biz_score_total (student_id, exam_id, total_score, score_rate, class_rank, grade_rank, calc_time) VALUES
('20230101', 'E06', 670, 0.8933, 1, 15, NOW()),
('20230102', 'E06', 611, 0.8147, 3, 58, NOW()),
('20230103', 'E06', 530, 0.7067, 6, 196, NOW()),
('20230104', 'E06', 615, 0.8200, 2, 54, NOW()),
('20230105', 'E06', 607, 0.8093, 4, 63, NOW()),
('20230106', 'E06', 543, 0.7240, 5, 168, NOW());

-- 一条待受理申请 + 一条已受理（维持）+ 一条驳回
INSERT INTO biz_appeal (appeal_id, score_id, student_id, exam_id, subject_id, original_score, reason, apply_time, status, handle_result, corrected_score, opinion, handler_id, handle_time, create_by, create_time) VALUES
('AP20260410001', 'SC20230101E06MATH', '20230101', 'E06', 'MATH', 121,
 '最后一道解答题我写了完整过程，但公布分数比预期低了十几分，申请复核该题计分。',
 '2027-04-12 14:30:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '20230101', NOW()),
('AP20251206001', NULL, '20230102', 'E04', 'CHEMISTRY', 78,
 '化学选择题第五题我选了 C，公布答案是 B，但题目条件似乎支持 C，请求复核。',
 '2025-12-06 15:40:00', 'ACCEPTED', 'KEEP', NULL,
 '已调阅原卷与标准答案逐题复核，第五题题干条件明确指向 B，计分无误，维持原成绩。',
 'T001', '2025-12-07 10:15:00', '20230102', NOW()),
('AP20251108001', NULL, '20230105', 'E03', 'POLITICS', 81,
 '政治主观题得分明显低于平时水平，怀疑分数录入有误，申请核查。',
 '2025-11-08 11:30:00', 'REJECTED', NULL, NULL,
 '经核查登分表与答题卡，分数录入无误；主观题失分点为未结合材料分析，驳回申请。',
 'T001', '2025-11-09 09:00:00', '20230105', NOW());

INSERT INTO biz_score_log (log_id, score_id, student_id, exam_id, subject_id, old_score, new_score, change_reason, operator_id, operate_time, source, appeal_id) VALUES
('LOG20251206001', 'SC20230103E04PHYSICS', '20230103', 'E04', 'PHYSICS', 55, 62,
 '登分时看错行，将 62 录成了 55，据实更正', 'T001', '2025-12-06 16:20:00', 'MANUAL', NULL),
('LOG20251109001', 'SC20230104E03BIOLOGY', '20230104', 'E03', 'BIOLOGY', 71, 76,
 '查分受理更正：经复核实验设计题第二问应得满分，已更正', 'T001', '2025-11-09 14:05:00', 'APPEAL', 'AP20251109001');
