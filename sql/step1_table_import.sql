-- =========================================================
-- 人员信息管理系统 - 业务表初始化脚本（修复版）
--
-- 执行前置：已创建数据库 table_import
-- 本脚本可独立执行，包含：
--   1) 四张业务原始表（若已存在则跳过）
--   2) 三张系统业务表
--   3) 两个只读视图
--
-- 说明：xsxj_xs_* 四张表原本由业务方提供数据，
--       此处在脚本中一并建好结构，保证新环境可直接执行。
-- =========================================================

USE `table_import`;

SET NAMES utf8mb4;

-- =========================================================
-- 一、四张业务原始表（只建结构，数据由业务导入）
-- =========================================================

-- 1.1 学籍基础信息（CSV 宽表，导入中间表）
CREATE TABLE IF NOT EXISTS `xsxj_xs_jbxx_compare_temp` (
  `id` int DEFAULT NULL,
  `sheng_mc_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `shi_mc_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `qu_mc_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `xuexiao_mc_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `nianji_mc_csv` int DEFAULT NULL,
  `banji_mc_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `xingming_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zjlx_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zjhm_csv` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `sheng_dm` int DEFAULT NULL,
  `sheng_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `shi_dm` int DEFAULT NULL,
  `shi_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `qu_dm` int DEFAULT NULL,
  `qu_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `jg_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `jg_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `xq_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `xq_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `nj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `nj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `bj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `bj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `is_deal` int DEFAULT NULL,
  `xm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `cjsj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `gxsj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  KEY `idx_zjhm_csv` (`zjhm_csv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='学籍基础信息（导入中间表）';

-- 1.2 学籍比对结果（系统查询在校状态的来源）
CREATE TABLE IF NOT EXISTS `xsxj_xs_jbxx_compare_temp_result` (
  `SFZJH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '身份证件号',
  `XM` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '姓名',
  `ZXSZT` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '在校状态',
  `XXMC` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学校名称',
  KEY `idx_sfzjh` (`SFZJH`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='学籍比对结果';

-- 1.3 转学信息（转出/转入学校明细）
CREATE TABLE IF NOT EXISTS `xsxj_xs_zxxx_temp` (
  `id` int DEFAULT NULL,
  `compare_temp_id` int DEFAULT NULL,
  `zjhm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '身份证件号',
  `xm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '姓名',
  `xsdm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zxlx` int DEFAULT NULL COMMENT '转学类型',
  `zr_sheng_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_sheng_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_shi_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_shi_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_qu_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_qu_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_jg_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_jg_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '转出学校',
  `zr_nj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_nj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_bj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zr_bj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_sheng_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_sheng_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_shi_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_shi_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_qu_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_qu_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_jg_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_jg_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '转入学校',
  `zc_nj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_nj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_bj_dm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zc_bj_mc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `zxsqsj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '转学申请时间',
  `cjsj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `gxsj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  KEY `idx_zjhm` (`zjhm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='转学信息（导入中间表）';

-- 1.4 转学比对结果
CREATE TABLE IF NOT EXISTS `xsxj_xs_zxxx_temp_result` (
  `SFZJH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '身份证件号',
  `XM` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '姓名',
  `ZXSZT` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '在校状态',
  `XXMC` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学校名称',
  KEY `idx_sfzjh` (`SFZJH`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='转学比对结果';

-- =========================================================
-- 二、系统业务表
-- =========================================================

-- 2.1 IP 授权表（用户与 IP 的对应关系，支持 CIDR 与审核）
CREATE TABLE IF NOT EXISTS `biz_ip_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '接入用户ID（sys_user.user_id）',
  `ip_address` varchar(64) NOT NULL COMMENT '授权IP或CIDR，如1.2.3.4、1.2.3.0/24',
  `source_type` char(1) NOT NULL DEFAULT '1' COMMENT '来源（1注册/首登 2自助添加 3管理员）',
  `auth_type` char(1) NOT NULL DEFAULT '1' COMMENT '授权类型（1白名单）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0有效 1停用）',
  `audit_status` char(1) NOT NULL DEFAULT '1' COMMENT '审核状态（0待审核 1通过 2驳回）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核意见',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间，NULL表示长期有效',
  `max_qps` int NOT NULL DEFAULT 20 COMMENT '每秒最大请求数',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_ip` (`user_id`,`ip_address`),
  KEY `idx_ip_status` (`ip_address`,`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='IP授权表';

-- 2.2 人员信息比对访问日志表
CREATE TABLE IF NOT EXISTS `biz_compare_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '请求链路ID',
  `user_id` bigint DEFAULT NULL COMMENT '接入用户ID',
  `login_name` varchar(64) DEFAULT NULL COMMENT '接入用户登录名快照',
  `ip_address` varchar(64) NOT NULL COMMENT '请求IP',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
  `query_name` varchar(100) DEFAULT NULL COMMENT '查询姓名',
  `query_id_card` varchar(64) DEFAULT NULL COMMENT '查询证件号（脱敏）',
  `query_id_card_hash` char(64) DEFAULT NULL COMMENT '标准化证件号SHA-256，便于检索',
  `query_params` json DEFAULT NULL COMMENT '原始请求参数',
  `match_count` int NOT NULL DEFAULT 0 COMMENT '命中结果条数',
  `is_in_school` char(1) DEFAULT NULL COMMENT '是否在读（1是 0否 空未知）',
  `student_type` varchar(20) DEFAULT NULL COMMENT '学生类型（PRIMARY_SECONDARY/VOCATIONAL/BOTH/UNKNOWN）',
  `is_target_student` char(1) DEFAULT NULL COMMENT '是否中小学生或职业学生（1是 0否）',
  `has_transfer` char(1) DEFAULT NULL COMMENT '是否有转学记录（1是 0否）',
  `conflict_flag` char(1) NOT NULL DEFAULT '0' COMMENT '是否存在冲突（0否 1是）',
  `need_review` char(1) NOT NULL DEFAULT '0' COMMENT '是否需人工复核（0否 1是）',
  `result_code` varchar(20) DEFAULT NULL COMMENT '结果码',
  `result_msg` varchar(500) DEFAULT NULL COMMENT '结果说明',
  `result_json` json DEFAULT NULL COMMENT '结果快照，含原始命中明细',
  `cost_ms` int DEFAULT NULL COMMENT '耗时毫秒',
  `request_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '请求时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`request_time`),
  KEY `idx_ip_time` (`ip_address`,`request_time`),
  KEY `idx_idcard_time` (`query_id_card_hash`,`request_time`),
  KEY `idx_name_time` (`query_name`,`request_time`),
  KEY `idx_trace` (`trace_id`),
  KEY `idx_request_time` (`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='人员信息比对访问日志表';

-- =========================================================
-- 三、只读视图（统一两张 result 表）
-- =========================================================

-- 3.1 学生结果统一视图
CREATE OR REPLACE VIEW `v_cmp_student_result` AS
SELECT
  `id_no`,
  `name`,
  `status`,
  `school_name`,
  GROUP_CONCAT(DISTINCT `source_type` ORDER BY `source_type` SEPARATOR ',') AS `source_types`
FROM (
  SELECT
    'JBXX' AS `source_type`,
    UPPER(TRIM(`SFZJH`)) AS `id_no`,
    TRIM(BOTH '"' FROM TRIM(`XM`)) AS `name`,
    TRIM(`ZXSZT`) AS `status`,
    TRIM(`XXMC`) AS `school_name`
  FROM `xsxj_xs_jbxx_compare_temp_result`
  WHERE `SFZJH` IS NOT NULL AND TRIM(`SFZJH`) <> ''

  UNION ALL

  SELECT
    'ZXXX' AS `source_type`,
    UPPER(TRIM(`SFZJH`)) AS `id_no`,
    TRIM(BOTH '"' FROM TRIM(`XM`)) AS `name`,
    TRIM(`ZXSZT`) AS `status`,
    TRIM(`XXMC`) AS `school_name`
  FROM `xsxj_xs_zxxx_temp_result`
  WHERE `SFZJH` IS NOT NULL AND TRIM(`SFZJH`) <> ''
) t
GROUP BY `id_no`, `name`, `status`, `school_name`;

-- 3.2 转学明细只读视图
CREATE OR REPLACE VIEW `v_cmp_transfer` AS
SELECT
  UPPER(TRIM(`zjhm`)) AS `id_no`,
  TRIM(`xm`) AS `name`,
  `zxlx`,
  `zr_jg_mc` AS `from_school`,
  `zc_jg_mc` AS `to_school`,
  `zxsqsj` AS `apply_time`
FROM `xsxj_xs_zxxx_temp`
WHERE `zjhm` IS NOT NULL AND TRIM(`zjhm`) <> '';

-- =========================================================
-- 执行完成
-- 说明：
--   1. 索引已内联在 CREATE TABLE 中，无需再执行 ALTER TABLE ADD INDEX
--   2. 若业务表已存在（含数据），本脚本不会覆盖
--   3. 新装环境此时四张业务表为空，可用 09/10 测试数据验证功能
-- =========================================================
