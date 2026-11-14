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
