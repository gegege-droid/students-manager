-- 数据导入后的自检
select 'classroom' as item, count(*) as cnt from edu_classroom
union all select 'student', count(*) from edu_student
union all select 'teacher', count(*) from edu_teacher
union all select 'course', count(*) from edu_course
union all select 'score', count(*) from edu_score
union all select 'appeal', count(*) from edu_appeal
union all select 'change_log', count(*) from edu_score_change_log;

select user_id, user_name, nick_name, status, del_flag from sys_user
 where user_id in (1001, 1002, 1003, 1004);

select config_key, config_value from sys_config
 where config_key = 'sys.account.captchaEnabled';

select menu_id, menu_name, parent_id, path, component, menu_type, perms
  from sys_menu where parent_id = 2000 or menu_id = 2000 order by menu_id;
