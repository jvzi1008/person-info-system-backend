-- P5：独立测试数据集（20 名虚拟学生）
-- 目的：一份数据里同时包含「学生结果」和「转学记录」，覆盖各种判定场景
--       xsxj_xs_test         → 学生信息 + 转学记录（对应 xsxj_xs_zxxx_temp 的角色）
--       xsxj_xs_test_result  → 学生状态结果（对应 xsxj_xs_*_result 的角色）
USE `table_import`;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `xsxj_xs_test`;
CREATE TABLE `xsxj_xs_test` (
  `id` int DEFAULT NULL,
  `zjhm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '证件号',
  `xm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '姓名',
  `xb` char(1) DEFAULT NULL COMMENT '性别（1男 2女）',
  `csrq` varchar(20) DEFAULT NULL COMMENT '出生日期',
  `xxmc` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校名称',
  `zxszt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '在校状态',
  `nj_mc` varchar(50) DEFAULT NULL COMMENT '年级',
  `bj_mc` varchar(50) DEFAULT NULL COMMENT '班级',
  `zxlx` int DEFAULT NULL COMMENT '转学类型（NULL=无转学）',
  `zr_jg_mc` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '转出学校',
  `zc_jg_mc` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '转入学校',
  `zxsqsj` varchar(50) DEFAULT NULL COMMENT '转学申请时间',
  `cjsj` varchar(50) DEFAULT NULL COMMENT '创建时间',
  KEY `idx_test_zjhm` (`zjhm`),
  KEY `idx_test_zxlx` (`zxlx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用学生信息与转学记录';

DROP TABLE IF EXISTS `xsxj_xs_test_result`;
CREATE TABLE `xsxj_xs_test_result` (
  `SFZJH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证件号',
  `XM` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '姓名',
  `ZXSZT` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '在校状态',
  `XXMC` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学校名称',
  KEY `idx_test_sfzjh` (`SFZJH`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用学生结果';
