-- =========================================================
-- P5：菜单调整（排序 + 新增用户审核入口 + 清理已下线菜单）
-- 原则：不改原生菜单的层级与路径，避免破坏前端硬编码（/system/user 等）
-- 本脚本可重复执行
-- =========================================================
USE `table_import`;
SET NAMES utf8mb4;

-- =========================================================
-- 一、清理已下线的「接入用户」菜单
--   biz_ip_user 表已在 P3 删除，对应 Controller 也已移除，
--   但 04_menu_compare_final.sql 仍会创建 3001/3004-3007，此处统一删除
-- =========================================================
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (3001, 3004, 3005, 3006, 3007);
DELETE FROM `sys_menu`      WHERE `menu_id` IN (3001, 3004, 3005, 3006, 3007);

-- 清理可能残留的孤儿授权（菜单已不存在的 role_menu）
DELETE rm FROM `sys_role_menu` rm
LEFT JOIN `sys_menu` m ON m.`menu_id` = rm.`menu_id`
WHERE m.`menu_id` IS NULL;

-- =========================================================
-- 二、调整顺序，常用功能靠前
-- =========================================================
UPDATE `sys_menu` SET `order_num` = 1 WHERE `menu_id` = 3000;   -- 人员信息管理
UPDATE `sys_menu` SET `order_num` = 2 WHERE `menu_id` = 108;    -- 日志管理
UPDATE `sys_menu` SET `order_num` = 3 WHERE `menu_id` = 1;      -- 系统管理
UPDATE `sys_menu` SET `order_num` = 4 WHERE `menu_id` = 2;      -- 系统监控
UPDATE `sys_menu` SET `order_num` = 5 WHERE `menu_id` = 3;      -- 系统工具

-- =========================================================
-- 三、新增「用户审核」快捷入口
--   复用 system/user/index 页面，通过 query 默认筛选待审核
--   原生「系统管理 → 用户管理」保持不变
-- =========================================================
DELETE FROM `sys_role_menu` WHERE `menu_id` = 3030;
DELETE FROM `sys_menu`      WHERE `menu_id` = 3030;

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
   `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES
  (3030, '用户审核', 3000, 2, 'userAudit', 'system/user/index', '{"auditStatus":"0"}', 'CompareUserAudit',
   1, 0, 'C', '0', '0', 'system:user:audit', 'user', 'admin', NOW(), '注册用户审核快捷入口（复用用户管理页）');

-- 仅超管可见
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 3030
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 3030);

-- IP授权 排在用户审核之后
UPDATE `sys_menu` SET `order_num` = 3 WHERE `menu_id` = 3002;

-- =========================================================
-- 执行完成
-- =========================================================
