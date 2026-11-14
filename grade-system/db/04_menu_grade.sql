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
