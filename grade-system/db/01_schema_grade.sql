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
