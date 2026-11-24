-- =========================================================
-- P1：身份统一 —— 审核字段、普通用户角色、权限
--
-- 本脚本可重复执行（所有结构变更均先判断是否存在）
-- 覆盖：sys_user 审核字段、biz_ip_auth 审核字段、普通用户角色、审核权限
-- =========================================================
USE `table_import`;
SET NAMES utf8mb4;

-- =========================================================
-- 一、sys_user 增加审核字段（幂等）
-- =========================================================

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_status');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `audit_status` char(1) NOT NULL DEFAULT ''1'' COMMENT ''审核状态（0待审核 1通过 2驳回）'' AFTER `status`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_by');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `audit_by` varchar(64) DEFAULT '''' COMMENT ''审核人'' AFTER `audit_status`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_time');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT ''审核时间'' AFTER `audit_by`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_remark');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `audit_remark` varchar(500) DEFAULT '''' COMMENT ''审核意见'' AFTER `audit_time`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 存量用户一律视为已通过审核
UPDATE `sys_user` SET `audit_status` = '1' WHERE `audit_status` IS NULL OR `audit_status` = '';

-- =========================================================
-- 二、biz_ip_auth 增加审核字段（幂等）
--    P2 引入 IP 审核功能，旧库若已建表则不会自动补列，这里统一处理
-- =========================================================

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_ip_auth' AND COLUMN_NAME = 'audit_status');
SET @s := IF(@c = 0,
  'ALTER TABLE `biz_ip_auth` ADD COLUMN `audit_status` char(1) NOT NULL DEFAULT ''1'' COMMENT ''审核状态（0待审核 1通过 2驳回）'' AFTER `status`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_ip_auth' AND COLUMN_NAME = 'audit_by');
SET @s := IF(@c = 0,
  'ALTER TABLE `biz_ip_auth` ADD COLUMN `audit_by` varchar(64) DEFAULT '''' COMMENT ''审核人'' AFTER `audit_status`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_ip_auth' AND COLUMN_NAME = 'audit_time');
SET @s := IF(@c = 0,
  'ALTER TABLE `biz_ip_auth` ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT ''审核时间'' AFTER `audit_by`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_ip_auth' AND COLUMN_NAME = 'audit_remark');
SET @s := IF(@c = 0,
  'ALTER TABLE `biz_ip_auth` ADD COLUMN `audit_remark` varchar(500) DEFAULT '''' COMMENT ''审核意见'' AFTER `audit_time`',
  'SET @__noop = 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 存量 IP 授权一律视为已通过（避免升级后全部失效）
UPDATE `biz_ip_auth` SET `audit_status` = '1' WHERE `audit_status` IS NULL OR `audit_status` = '';

-- =========================================================
-- 三、普通用户角色
-- =========================================================

INSERT INTO `sys_role`
  (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
   `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '普通用户', 'normal', 3, '5', 1, 1, '0', '0', 'admin', NOW(), '外部注册用户默认角色'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'normal');

-- =========================================================
-- 四、权限绑定
-- =========================================================

-- 普通用户角色：人员信息比对权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, 3015
FROM `sys_role` r
WHERE r.`role_key` = 'normal'
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = r.`role_id` AND rm.`menu_id` = 3015);

-- 管理员审核按钮权限（3020）
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
   `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3020, '用户审核', 100, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0',
       'system:user:audit', '#', 'admin', NOW(), '注册用户审核'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 3020);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 3020
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 3020);

-- =========================================================
-- 执行完成
-- =========================================================
