-- P4：比对访问日志归档表
-- 目的：主表只保留近期数据，旧数据转入历史表，避免无限增长
USE `table_import`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `biz_compare_access_log_archive` (
  `id` bigint NOT NULL COMMENT '原日志ID',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '请求链路ID',
  `user_id` bigint DEFAULT NULL COMMENT '接入用户ID',
  `login_name` varchar(64) DEFAULT NULL COMMENT '登录名快照',
  `ip_address` varchar(64) NOT NULL COMMENT '请求IP',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
  `query_name` varchar(100) DEFAULT NULL COMMENT '查询姓名',
  `query_id_card` varchar(64) DEFAULT NULL COMMENT '查询证件号（脱敏）',
  `query_id_card_hash` char(64) DEFAULT NULL COMMENT '证件号SHA-256',
  `query_params` json DEFAULT NULL COMMENT '原始请求参数',
  `match_count` int NOT NULL DEFAULT 0 COMMENT '命中条数',
  `is_in_school` char(1) DEFAULT NULL COMMENT '是否在读',
  `student_type` varchar(20) DEFAULT NULL COMMENT '学生类型',
  `is_target_student` char(1) DEFAULT NULL COMMENT '是否中小学生或职业学生',
  `has_transfer` char(1) DEFAULT NULL COMMENT '是否有转学记录',
  `conflict_flag` char(1) NOT NULL DEFAULT '0' COMMENT '是否冲突',
  `need_review` char(1) NOT NULL DEFAULT '0' COMMENT '是否需复核',
  `result_code` varchar(20) DEFAULT NULL COMMENT '结果码',
  `result_msg` varchar(500) DEFAULT NULL COMMENT '结果说明',
  `result_json` json DEFAULT NULL COMMENT '结果快照',
  `cost_ms` int DEFAULT NULL COMMENT '耗时毫秒',
  `request_time` datetime(3) NOT NULL COMMENT '请求时间',
  `create_time` datetime NOT NULL COMMENT '入库时间',
  `archived_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  PRIMARY KEY (`id`),
  KEY `idx_arch_user_time` (`user_id`,`request_time`),
  KEY `idx_arch_request_time` (`request_time`),
  KEY `idx_arch_idcard_time` (`query_id_card_hash`,`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='比对访问日志归档表';
