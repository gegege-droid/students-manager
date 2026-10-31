-- ----------------------------------------------------------------------------
-- 由 ruoyi-generator 生成的菜单 SQL（在其基础上补一个「成绩管理」一级目录）
-- 生成配置：表 biz_score → 实体 Score，包 com.ruoyi.business，模块 business，功能名「月考成绩」
-- ----------------------------------------------------------------------------

-- 一级目录：成绩管理
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成绩管理', '0', '4', 'business', null, 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '成绩管理目录');

SELECT @dirId := LAST_INSERT_ID();

-- 菜单：月考成绩（由生成器产出，父级改为上面的一级目录）
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩', @dirId, '1', 'score', 'business/score/index', 1, 0, 'C', '0', '0', 'business:score:list', '#', 'admin', sysdate(), '', null, '月考成绩菜单');

SELECT @parentId := LAST_INSERT_ID();

-- 按钮权限
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩查询', @parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'business:score:query', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩新增', @parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'business:score:add', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩修改', @parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'business:score:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩删除', @parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'business:score:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('月考成绩导出', @parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'business:score:export', '#', 'admin', sysdate(), '', null, '');
