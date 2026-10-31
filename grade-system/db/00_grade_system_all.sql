-- ============================================================
-- 成绩管理系统 一键初始化脚本
-- 使用方法：先创建/选择数据库 ry-vue，再整体执行本文件
-- ============================================================


-- ==================== 01_schema_grade.sql ====================
-- ============================================================
-- 成绩管理系统 业务表结构（MySQL 5.7 兼容）
-- 说明：不建立物理外键，引用完整性由 Service 层保证
-- ============================================================

drop table if exists edu_score_change_log;
drop table if exists edu_appeal;
drop table if exists edu_score;
drop table if exists edu_course;
drop table if exists edu_student;
drop table if exists edu_teacher;
drop table if exists edu_classroom;

create table edu_classroom (
  class_id        varchar(16)   not null                  comment '班级编号',
  class_name      varchar(50)   not null                  comment '班级名称',
  grade           varchar(16)   default ''                comment '年级',
  head_teacher_id varchar(16)   default ''                comment '班主任工号',
  status          char(1)       default '0'               comment '状态（0正常 1停用）',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (class_id),
  unique key uk_class_name (class_name)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='班级信息表';

create table edu_teacher (
  teacher_id      varchar(16)   not null                  comment '教师工号',
  teacher_name    varchar(30)   not null                  comment '教师姓名',
  title           varchar(30)   default ''                comment '职称',
  phone           varchar(11)   default ''                comment '手机号',
  status          char(1)       default '0'               comment '状态（0正常 1停用）',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (teacher_id)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='教师信息表';

create table edu_student (
  student_id      varchar(16)   not null                  comment '学号',
  student_name    varchar(30)   not null                  comment '学生姓名',
  class_id        varchar(16)   not null                  comment '班级编号',
  gender          char(1)       default '0'               comment '性别（0男 1女 2未知）',
  phone           varchar(11)   default ''                comment '手机号',
  status          char(1)       default '0'               comment '状态（0正常 1停用）',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (student_id),
  key idx_student_class (class_id)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='学生信息表';

create table edu_course (
  course_id       varchar(16)   not null                  comment '课程编号',
  course_name     varchar(50)   not null                  comment '课程名称',
  credit          decimal(3,1)  not null                  comment '学分',
  term            varchar(20)   not null                  comment '学期（如2024-2025-1）',
  teacher_id      varchar(16)   not null                  comment '任课教师工号',
  course_type     varchar(16)   default 'REQUIRED'        comment '课程类型（REQUIRED必修 ELECTIVE选修）',
  status          char(1)       default '0'               comment '状态（0正常 1停用）',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (course_id),
  unique key uk_course_name_term (course_name, term),
  key idx_course_teacher (teacher_id)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='课程信息表';

create table edu_score (
  score_id        varchar(32)   not null                  comment '成绩编号',
  student_id      varchar(16)   not null                  comment '学号',
  course_id       varchar(16)   not null                  comment '课程编号',
  term            varchar(20)   not null                  comment '学期',
  usual_score     int(3)        default null              comment '平时成绩（0-100）',
  final_score     int(3)        default null              comment '期末成绩（0-100）',
  total_score     int(3)        default null              comment '总评成绩',
  gpa             decimal(3,1)  default null              comment '绩点',
  manual_total    char(1)       default '0'               comment '总评是否人工更正（0否 1是）',
  recorder_id     varchar(16)   default ''                comment '录入人工号',
  record_time     datetime                                comment '录入时间',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (score_id),
  unique key uk_score_stu_course_term (student_id, course_id, term),
  key idx_score_course_term (course_id, term),
  key idx_score_student_term (student_id, term)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='学生成绩表';

create table edu_appeal (
  appeal_id       varchar(32)   not null                  comment '申请编号',
  student_id      varchar(16)   not null                  comment '学号',
  course_id       varchar(16)   not null                  comment '课程编号',
  term            varchar(20)   not null                  comment '学期',
  original_score  int(3)        default null              comment '申请时原总评成绩',
  reason          varchar(300)  not null                  comment '申请理由（10-200字）',
  apply_time      datetime      not null                  comment '申请时间',
  status          varchar(16)   not null default 'PENDING' comment '状态（PENDING待受理 ACCEPTED已受理 REJECTED已驳回）',
  result          varchar(16)   default ''                comment '处理结果（KEEP维持 CORRECT更正）',
  corrected_score int(3)        default null              comment '更正后成绩',
  opinion         varchar(300)  default ''                comment '处理意见（5-200字）',
  handler_id      varchar(16)   default ''                comment '受理人工号',
  handle_time     datetime                                comment '受理时间',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (appeal_id),
  key idx_appeal_student (student_id),
  key idx_appeal_status_time (status, apply_time),
  key idx_appeal_course (course_id, term)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='查分申请表';

create table edu_score_change_log (
  log_id          varchar(32)   not null                  comment '日志编号',
  score_id        varchar(32)   not null                  comment '成绩编号',
  student_id      varchar(16)   not null                  comment '学号',
  course_id       varchar(16)   not null                  comment '课程编号',
  term            varchar(20)   not null                  comment '学期',
  old_usual       int(3)        default null              comment '原平时成绩',
  old_final       int(3)        default null              comment '原期末成绩',
  old_total       int(3)        default null              comment '原总评成绩',
  new_usual       int(3)        default null              comment '新平时成绩',
  new_final       int(3)        default null              comment '新期末成绩',
  new_total       int(3)        default null              comment '新总评成绩',
  change_reason   varchar(200)  not null                  comment '修改原因（5-100字）',
  operator_id     varchar(16)   not null                  comment '操作人工号',
  operate_time    datetime      not null                  comment '操作时间',
  source          varchar(16)   not null                  comment '来源（MANUAL手工 APPEAL查分受理）',
  create_by       varchar(64)   default ''                comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       varchar(64)   default ''                comment '更新者',
  update_time     datetime                                comment '更新时间',
  remark          varchar(500)  default null              comment '备注',
  primary key (log_id),
  key idx_log_score (score_id),
  key idx_log_student_course (student_id, course_id)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='成绩修改记录表';

-- ==================== 02_seed_grade.sql ====================
-- ============================================================
-- 成绩管理系统 演示种子数据
-- 与实验一 grade-front/src/api/mock.js 保持一致
-- total_score / gpa 已按公式计算好：
--   总评 = round(平时*0.4 + 期末*0.6)
--   绩点 = 总评<60 ? 0 : (总评-50)/10
-- ============================================================
set names utf8mb4;

delete from edu_score_change_log;
delete from edu_appeal;
delete from edu_score;
delete from edu_course;
delete from edu_student;
delete from edu_teacher;
delete from edu_classroom;

insert into edu_classroom
(class_id, class_name, grade, head_teacher_id, status, create_by, create_time, remark)
values
('G2101', '高一(1)班', '高一', 'T001', '0', 'admin', '2025-01-01 09:00:00', '演示班级');

insert into edu_teacher
(teacher_id, teacher_name, title, phone, status, create_by, create_time, remark)
values
('T001', '李老师', '班主任', '13800000001', '0', 'admin', '2025-01-01 09:00:00', '演示教师');

insert into edu_student
(student_id, student_name, class_id, gender, phone, status, create_by, create_time, remark)
values
('2021001', '张三', 'G2101', '0', '', '0', 'admin', '2025-01-01 09:00:00', ''),
('2021002', '李四', 'G2101', '0', '', '0', 'admin', '2025-01-01 09:00:00', ''),
('2021003', '王五', 'G2101', '1', '', '0', 'admin', '2025-01-01 09:00:00', ''),
('2021004', '赵六', 'G2101', '0', '', '0', 'admin', '2025-01-01 09:00:00', ''),
('2021005', '钱七', 'G2101', '1', '', '0', 'admin', '2025-01-01 09:00:00', ''),
('2021006', '孙八', 'G2101', '0', '', '0', 'admin', '2025-01-01 09:00:00', '');

insert into edu_course
(course_id, course_name, credit, term, teacher_id, course_type, status, create_by, create_time, remark)
values
('CHN101', '语文', 5.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('MAT101', '数学', 5.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('ENG101', '英语', 5.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('PHY101', '物理', 4.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('CHM101', '化学', 4.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('BIO101', '生物', 4.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('POL101', '政治', 3.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('HIS101', '历史', 3.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', ''),
('GEO101', '地理', 3.0, '2024-2025-1', 'T001', 'REQUIRED', '0', 'admin', '2025-01-01 09:00:00', '');

insert into edu_score
(score_id, student_id, course_id, term, usual_score, final_score, total_score, gpa, manual_total,
 recorder_id, record_time, create_by, create_time, remark)
values
('SC20250101001', '2021001', 'CHN101', '2024-2025-1', 85, 92, 89, 3.9, '0', 'T001', '2025-01-10 16:20:00', 'T001', '2025-01-10 16:20:00', ''),
('SC20250101002', '2021001', 'MAT101', '2024-2025-1', 78, 82, 80, 3.0, '0', 'T001', '2025-01-10 16:22:00', 'T001', '2025-01-10 16:22:00', ''),
('SC20250101003', '2021001', 'ENG101', '2024-2025-1', 66, 72, 70, 2.0, '0', 'T001', '2025-01-10 16:24:00', 'T001', '2025-01-10 16:24:00', ''),
('SC20250101004', '2021001', 'PHY101', '2024-2025-1', 58, 64, 62, 1.2, '0', 'T001', '2025-01-10 16:26:00', 'T001', '2025-01-10 16:26:00', ''),
('SC20250101005', '2021001', 'CHM101', '2024-2025-1', 91, 95, 93, 4.3, '0', 'T001', '2025-01-10 16:28:00', 'T001', '2025-01-10 16:28:00', ''),
('SC20250101006', '2021001', 'BIO101', '2024-2025-1', 88, 86, 87, 3.7, '0', 'T001', '2025-01-10 16:30:00', 'T001', '2025-01-10 16:30:00', ''),
('SC20250101007', '2021001', 'POL101', '2024-2025-1', 95, 90, 92, 4.2, '0', 'T001', '2025-01-10 16:32:00', 'T001', '2025-01-10 16:32:00', ''),
('SC20250101008', '2021001', 'HIS101', '2024-2025-1', 52, 48, 50, 0.0, '0', 'T001', '2025-01-10 16:34:00', 'T001', '2025-01-10 16:34:00', ''),
('SC20250101009', '2021001', 'GEO101', '2024-2025-1', 82, 88, 86, 3.6, '0', 'T001', '2025-01-10 16:36:00', 'T001', '2025-01-10 16:36:00', ''),
('SC20250101011', '2021003', 'CHN101', '2024-2025-1', 76, 81, 79, 2.9, '0', 'T001', '2025-01-10 16:40:00', 'T001', '2025-01-10 16:40:00', ''),
('SC20250101012', '2021003', 'ENG101', '2024-2025-1', 66, 72, 70, 2.0, '0', 'T001', '2025-01-10 16:42:00', 'T001', '2025-01-10 16:42:00', ''),
('SC20250101021', '2021005', 'PHY101', '2024-2025-1', 58, 64, 62, 1.2, '0', 'T001', '2025-01-10 16:46:00', 'T001', '2025-01-10 16:46:00', '');

insert into edu_appeal
(appeal_id, student_id, course_id, term, original_score, reason, apply_time, status, result,
 corrected_score, opinion, handler_id, handle_time, create_by, create_time, remark)
values
('AP20250115001', '2021001', 'HIS101', '2024-2025-1', 50,
 '平时分已按要求提交全部作业，请老师复核登分情况', '2025-01-15 14:30:00',
 'PENDING', '', null, '', '', null, '2021001', '2025-01-15 14:30:00', ''),
('AP20250115002', '2021003', 'ENG101', '2024-2025-1', 70,
 '期末卷面估分78，与公布成绩差距较大', '2025-01-15 15:10:00',
 'PENDING', '', null, '', '', null, '2021003', '2025-01-15 15:10:00', ''),
('AP20250115003', '2021005', 'PHY101', '2024-2025-1', 62,
 '平时分一项显示为空，疑似漏录', '2025-01-15 18:45:00',
 'PENDING', '', null, '', '', null, '2021005', '2025-01-15 18:45:00', ''),
('AP20241220003', '2021001', 'CHN101', '2024-2025-1', 89,
 '期末成绩与预期差距较大', '2024-12-20 09:15:00',
 'ACCEPTED', 'KEEP', null, '已调阅答卷逐题复核，计分无误，维持原成绩。', 'T001', '2024-12-21 09:00:00',
 '2021001', '2024-12-20 09:15:00', ''),
('AP20241218001', '2021001', 'BIO101', '2024-2025-1', 87,
 '听力部分疑似未计入总分', '2024-12-18 16:40:00',
 'REJECTED', '', null, '听力成绩已计入，复核后确认计分无误，驳回申请。', 'T001', '2024-12-19 10:10:00',
 '2021001', '2024-12-18 16:40:00', '');

insert into edu_score_change_log
(log_id, score_id, student_id, course_id, term, old_usual, old_final, old_total,
 new_usual, new_final, new_total, change_reason, operator_id, operate_time, source,
 create_by, create_time, remark)
values
('LOG20250116001', 'SC20250101004', '2021001', 'PHY101', '2024-2025-1', 58, 60, 59,
 58, 64, 62, '登分时看错行', 'T001', '2025-01-08 15:05:00', 'MANUAL',
 'T001', '2025-01-08 15:05:00', ''),
('LOG20241221001', 'SC20250101001', '2021001', 'CHN101', '2024-2025-1', 80, 92, 87,
 85, 92, 89, '平时分漏计一次课堂作业，据实更正', 'T001', '2024-12-21 09:40:00', 'APPEAL',
 'T001', '2024-12-21 09:40:00', '');

-- ==================== 03_portal_accounts.sql ====================
-- ============================================================
-- 用户侧门户账号（复用 RuoYi sys_user / sys_role / sys_user_role）
-- 账号：2021001 / 2021003 / 2021005（学生），T001（教师）
-- 密码：123456（BCrypt，已用 Spring Security BCrypt.checkpw 验证）
-- 同时关闭登录验证码，便于门户登录
-- ============================================================
set names utf8mb4;

delete from sys_user_role where user_id in (1001, 1002, 1003, 1004);
delete from sys_user where user_id in (1001, 1002, 1003, 1004)
      or user_name in ('2021001', '2021003', '2021005', 'T001');
delete from sys_role where role_id in (100, 101);

insert into sys_role
(role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly,
 dept_check_strictly, status, del_flag, create_by, create_time, remark)
values
(100, '学生', 'student', 10, '1', 1, 1, '0', '0', 'admin', sysdate(), '成绩管理系统学生门户角色'),
(101, '教师', 'teacher', 11, '1', 1, 1, '0', '0', 'admin', sysdate(), '成绩管理系统教师门户角色');

insert into sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar,
 password, status, del_flag, login_ip, login_date, pwd_update_date,
 create_by, create_time, update_by, update_time, remark)
values
(1001, 100, '2021001', '张三', '00', '', '', '0', '',
 '$2a$10$Tcb4rIgj/hQ85vojIoiH1ewOTNKZamEz0/.aLmVk5e4u4V0POsG0K', '0', '0', '', null, sysdate(),
 'admin', sysdate(), '', null, '成绩管理系统学生账号'),
(1002, 100, '2021003', '王五', '00', '', '', '1', '',
 '$2a$10$Tcb4rIgj/hQ85vojIoiH1ewOTNKZamEz0/.aLmVk5e4u4V0POsG0K', '0', '0', '', null, sysdate(),
 'admin', sysdate(), '', null, '成绩管理系统学生账号'),
(1003, 100, '2021005', '钱七', '00', '', '', '1', '',
 '$2a$10$Tcb4rIgj/hQ85vojIoiH1ewOTNKZamEz0/.aLmVk5e4u4V0POsG0K', '0', '0', '', null, sysdate(),
 'admin', sysdate(), '', null, '成绩管理系统学生账号'),
(1004, 100, 'T001', '李老师', '00', '', '', '0', '',
 '$2a$10$Tcb4rIgj/hQ85vojIoiH1ewOTNKZamEz0/.aLmVk5e4u4V0POsG0K', '0', '0', '', null, sysdate(),
 'admin', sysdate(), '', null, '成绩管理系统教师账号');

insert into sys_user_role (user_id, role_id) values
(1001, 100),
(1002, 100),
(1003, 100),
(1004, 101);

-- 关闭验证码（RuoYi 默认从 sys_config 读取）
update sys_config set config_value = 'false'
 where config_key = 'sys.account.captchaEnabled';

-- ==================== 04_menu_grade.sql ====================
-- ============================================================
-- 管理端「成绩管理」目录 + ruoyi-generator 生成菜单
-- 可重复执行：先清理旧的生成菜单，再插入父目录和六个模块菜单
-- ============================================================
set names utf8mb4;

-- 清理旧生成菜单（包含菜单与按钮）
delete from sys_role_menu where menu_id in (select menu_id from sys_menu where perms like 'system:score:%' or perms like 'system:appeal:%' or perms like 'system:course:%' or perms like 'system:student:%' or perms like 'system:teacher:%' or perms like 'system:classroom:%');
delete from sys_menu where perms like 'system:score:%' or perms like 'system:appeal:%' or perms like 'system:course:%' or perms like 'system:student:%' or perms like 'system:teacher:%' or perms like 'system:classroom:%';
delete from sys_role_menu where menu_id = 2000;
delete from sys_menu where menu_id = 2000;

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
values
(2000, '成绩管理', 0, 5, 'grade', null, '', '', 1, 0, 'M', '0', '0', '', 'education',
 'admin', sysdate(), '', null, '成绩管理系统目录');

-- ===== 以下由 ruoyi-generator 生成 =====
-- scoreMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理', '2000', '1', 'score', 'system/score/index', 1, 0, 'C', '0', '0', 'system:score:list', '#', 'admin', sysdate(), '', null, '成绩管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:score:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:score:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:score:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:score:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:score:export',       '#', 'admin', sysdate(), '', null, '');

-- appealMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理', '2000', '1', 'appeal', 'system/appeal/index', 1, 0, 'C', '0', '0', 'system:appeal:list', '#', 'admin', sysdate(), '', null, '查分申请管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:appeal:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:appeal:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:appeal:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:appeal:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查分申请管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:appeal:export',       '#', 'admin', sysdate(), '', null, '');

-- courseMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理', '2000', '1', 'course', 'system/course/index', 1, 0, 'C', '0', '0', 'system:course:list', '#', 'admin', sysdate(), '', null, '课程管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:course:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:course:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:course:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:course:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('课程管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:course:export',       '#', 'admin', sysdate(), '', null, '');

-- studentMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理', '2000', '1', 'student', 'system/student/index', 1, 0, 'C', '0', '0', 'system:student:list', '#', 'admin', sysdate(), '', null, '学生管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:student:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:student:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:student:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:student:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:student:export',       '#', 'admin', sysdate(), '', null, '');

-- teacherMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理', '2000', '1', 'teacher', 'system/teacher/index', 1, 0, 'C', '0', '0', 'system:teacher:list', '#', 'admin', sysdate(), '', null, '教师管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:teacher:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:teacher:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:teacher:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:teacher:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('教师管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:teacher:export',       '#', 'admin', sysdate(), '', null, '');

-- classroomMenu.sql
-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理', '2000', '1', 'classroom', 'system/classroom/index', 1, 0, 'C', '0', '0', 'system:classroom:list', '#', 'admin', sysdate(), '', null, '班级管理菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:classroom:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:classroom:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:classroom:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:classroom:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('班级管理导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:classroom:export',       '#', 'admin', sysdate(), '', null, '');
