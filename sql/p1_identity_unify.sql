-- P1: identity unify - sys_user audit columns, normal role, permissions
USE `table_import`;
SET NAMES utf8mb4;

ALTER TABLE `sys_user`
  MODIFY COLUMN `user_name` varchar(64) NOT NULL COMMENT '用户账号（普通用户为邮箱）',
  ADD COLUMN `audit_status` char(1) NOT NULL DEFAULT '1' COMMENT '审核状态（0待审核 1通过 2驳回）' AFTER `status`,
  ADD COLUMN `audit_by` varchar(64) DEFAULT '' COMMENT '审核人' AFTER `audit_status`,
  ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT '审核时间' AFTER `audit_by`,
  ADD COLUMN `audit_remark` varchar(500) DEFAULT '' COMMENT '审核意见' AFTER `audit_time`;

UPDATE `sys_user` SET `audit_status` = '1' WHERE `audit_status` IS NULL OR `audit_status` = '';

INSERT INTO `sys_role`
  (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '普通用户', 'normal', 3, '5', 1, 1, '0', '0', 'admin', NOW(), '外部注册用户默认角色'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'normal');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, 3015 FROM `sys_role` r
WHERE r.`role_key` = 'normal'
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = r.`role_id` AND rm.`menu_id` = 3015);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3020, '用户审核', 100, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:audit', '#', 'admin', NOW(), '注册用户审核'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 3020);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 3020 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 3020);
