-- P5：菜单调整（方案 A 排序/隐藏 + 方案 B 新增用户审核入口）
-- 原则：不改原生菜单的层级与路径，避免破坏前端硬编码（/system/user 等）
USE `table_import`;
SET NAMES utf8mb4;

-- ========== 方案 A：调整顺序，常用功能靠前 ==========
-- 人员信息管理 排第 1
UPDATE `sys_menu` SET `order_num` = 1 WHERE `menu_id` = 3000;
-- 日志管理 排第 2
UPDATE `sys_menu` SET `order_num` = 2 WHERE `menu_id` = 108;
-- 系统管理 / 系统监控 / 系统工具 后置
UPDATE `sys_menu` SET `order_num` = 3 WHERE `menu_id` = 1;
UPDATE `sys_menu` SET `order_num` = 4 WHERE `menu_id` = 2;
UPDATE `sys_menu` SET `order_num` = 5 WHERE `menu_id` = 3;

-- ========== 方案 B：人员信息管理下新增「用户审核」快捷入口 ==========
-- 复用 system/user/index 页面，通过 query 默认筛选待审核；原生「用户管理」保持不变
DELETE FROM `sys_role_menu` WHERE `menu_id` = 3030;
DELETE FROM `sys_menu` WHERE `menu_id` = 3030;

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES
  (3030, '用户审核', 3000, 2, 'userAudit', 'system/user/index', '{"auditStatus":"0"}', 'CompareUserAudit', 1, 0, 'C', '0', '0', 'system:user:audit', 'user', 'admin', NOW(), '注册用户审核快捷入口（复用用户管理页）');

-- 仅超管可见
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 3030);

-- IP授权 排到用户审核之后
UPDATE `sys_menu` SET `order_num` = 3 WHERE `menu_id` = 3002;
