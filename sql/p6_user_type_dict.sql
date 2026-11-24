-- ========================================================
-- P6：用户类型规范化（字段注释 + 数据字典）
-- 幂等：可重复执行
-- ========================================================
USE `table_import`;
SET NAMES utf8mb4;

-- 1. 补全 sys_user.user_type 注释（原来只写了 00，01 是 P1 新增的）
ALTER TABLE `sys_user`
  MODIFY COLUMN `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户 01普通用户）';

-- 2. 字典类型：用户类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '用户类型', 'sys_user_type', '0', 'admin', NOW(), '用户类型（00系统用户 01普通用户）'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'sys_user_type');

-- 3. 字典数据：00 / 01
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '系统用户', '00', 'sys_user_type', '', 'primary', 'Y', '0', 'admin', NOW(), '内部账号，进入后台'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_user_type' AND `dict_value` = '00');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '普通用户', '01', 'sys_user_type', '', 'warning', 'N', '0', 'admin', NOW(), '外部注册用户，走外部界面'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_user_type' AND `dict_value` = '01');
