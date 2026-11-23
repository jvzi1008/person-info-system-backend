-- P1: migrate biz_ip_user -> sys_user (idempotent)
USE `table_import`;
SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS `p1_migrate_biz_users`;

DELIMITER $$
CREATE PROCEDURE `p1_migrate_biz_users`()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE old_id BIGINT;
  DECLARE v_login VARCHAR(64);
  DECLARE v_name VARCHAR(100);
  DECLARE v_pwd VARCHAR(100);
  DECLARE v_status CHAR(1);
  DECLARE v_email VARCHAR(128);
  DECLARE v_del CHAR(1);
  DECLARE new_id BIGINT;

  DECLARE cur CURSOR FOR
    SELECT id, login_name, user_name, password, status, contact_email, del_flag
    FROM biz_ip_user;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  DROP TEMPORARY TABLE IF EXISTS tmp_p1_map;
  CREATE TEMPORARY TABLE tmp_p1_map (old_id BIGINT PRIMARY KEY, new_id BIGINT);

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO old_id, v_login, v_name, v_pwd, v_status, v_email, v_del;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET new_id = NULL;
    SET new_id = (SELECT user_id FROM sys_user WHERE user_name = v_login LIMIT 1);

    IF new_id IS NULL THEN
      INSERT INTO sys_user
        (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password,
         status, audit_status, audit_by, audit_time, audit_remark, del_flag, create_by, create_time, remark)
      VALUES
        (NULL, v_login, LEFT(COALESCE(NULLIF(v_name, ''), v_login), 30), '01',
         LEFT(COALESCE(NULLIF(v_email, ''), IF(v_login LIKE '%@%', v_login, '')), 50),
         '', '2', '', v_pwd,
         COALESCE(NULLIF(v_status, ''), '0'), '1', 'migrate', NOW(), '存量迁移', COALESCE(NULLIF(v_del, ''), '0'),
         'migrate', NOW(), '由 biz_ip_user 迁移');
      SET new_id = LAST_INSERT_ID();
    ELSE
      UPDATE sys_user
         SET user_type = '01', audit_status = '1', password = v_pwd,
             del_flag = COALESCE(NULLIF(v_del, ''), '0')
       WHERE user_id = new_id;
    END IF;

    INSERT INTO tmp_p1_map (old_id, new_id) VALUES (old_id, new_id);

    INSERT INTO sys_user_role (user_id, role_id)
    SELECT new_id, r.role_id FROM sys_role r
    WHERE r.role_key = 'normal'
      AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = new_id AND ur.role_id = r.role_id);

    UPDATE biz_ip_auth SET user_id = new_id WHERE user_id = old_id;
  END LOOP;
  CLOSE cur;

  SELECT * FROM tmp_p1_map ORDER BY old_id;
END$$
DELIMITER ;

-- 仅当旧表 biz_ip_user 存在时才执行迁移；新库自动跳过
SET @has_legacy_table := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_ip_user'
);
SET @migrate_sql := IF(@has_legacy_table > 0,
  'CALL p1_migrate_biz_users()',
  'SELECT ''biz_ip_user 不存在，跳过存量账号迁移'' AS msg');
PREPARE migrate_stmt FROM @migrate_sql;
EXECUTE migrate_stmt;
DEALLOCATE PREPARE migrate_stmt;

DROP PROCEDURE IF EXISTS `p1_migrate_biz_users`;
