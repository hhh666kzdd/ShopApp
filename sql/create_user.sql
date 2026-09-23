-- =====================================================================
-- 创建 Android 端使用的数据库账号（MySQL 8.0）
-- 用法（以 root 登录后执行）：mysql -uroot -p < create_user.sql
--
-- 注意：Android 端使用 mysql-connector-java 5.1.49，不支持 MySQL 8 默认的
-- caching_sha2_password 认证方式，因此该账号必须使用 mysql_native_password。
-- 账号：shop / 123456，允许任意主机（%）连接，用于手机 / 模拟器通过局域网访问。
-- =====================================================================

CREATE USER IF NOT EXISTS 'shop'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
ALTER USER 'shop'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT ALL PRIVILEGES ON shop_db.* TO 'shop'@'%';
FLUSH PRIVILEGES;
