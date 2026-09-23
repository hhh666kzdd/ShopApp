-- =====================================================================
-- 购物管理系统（Android 课程设计）数据库脚本
-- 数据库：MySQL 5.7 / 8.0，库名 shop_db，字符集 utf8mb4
-- 用法：mysql -uroot -p < shop_db.sql
-- 说明：脚本可重复执行，会先删除已存在的表再重建，并写入测试数据
-- =====================================================================

CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE shop_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_promotion;
DROP TABLE IF EXISTS t_user_coupon;
DROP TABLE IF EXISTS t_coupon;
DROP TABLE IF EXISTS t_address;
DROP TABLE IF EXISTS t_cart;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_category;
DROP TABLE IF EXISTS t_user;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
CREATE TABLE t_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
  password    VARCHAR(64)  NOT NULL COMMENT 'MD5 密码（32 位小写）',
  nickname    VARCHAR(50)  COMMENT '昵称',
  phone       VARCHAR(20)  COMMENT '手机号',
  avatar      VARCHAR(255) COMMENT '头像 URL',
  role        TINYINT      NOT NULL DEFAULT 0 COMMENT '0 普通用户 1 管理员',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 2. 商品分类表
-- ---------------------------------------------------------------------
CREATE TABLE t_category (
  id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  name   VARCHAR(50)  NOT NULL COMMENT '分类名称',
  icon   VARCHAR(255) COMMENT '图标 URL',
  sort   INT          DEFAULT 0 COMMENT '排序值，越小越靠前',
  status TINYINT      DEFAULT 1 COMMENT '1 启用 0 停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ---------------------------------------------------------------------
-- 3. 商品表
-- ---------------------------------------------------------------------
CREATE TABLE t_product (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT        NOT NULL COMMENT '分类 ID',
  name        VARCHAR(100)  NOT NULL COMMENT '商品名称',
  subtitle    VARCHAR(200)  COMMENT '副标题 / 卖点',
  main_image  VARCHAR(255)  COMMENT '主图 URL',
  detail      TEXT          COMMENT '商品描述',
  price       DECIMAL(10,2) NOT NULL COMMENT '原价',
  stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
  sales       INT           NOT NULL DEFAULT 0 COMMENT '销量',
  status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ---------------------------------------------------------------------
-- 4. 购物车表（同一用户同一商品只有一条记录）
-- ---------------------------------------------------------------------
CREATE TABLE t_cart (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT  NOT NULL,
  product_id  BIGINT  NOT NULL,
  quantity    INT     NOT NULL DEFAULT 1,
  checked     TINYINT NOT NULL DEFAULT 1 COMMENT '1 勾选 0 未勾选',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ---------------------------------------------------------------------
-- 5. 收货地址表
-- ---------------------------------------------------------------------
CREATE TABLE t_address (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL,
  receiver_name  VARCHAR(50)  NOT NULL COMMENT '收货人',
  receiver_phone VARCHAR(20)  NOT NULL COMMENT '收货电话',
  province       VARCHAR(50)  NOT NULL COMMENT '省',
  city           VARCHAR(50)  NOT NULL COMMENT '市',
  district       VARCHAR(50)  NOT NULL COMMENT '区 / 县',
  detail         VARCHAR(200) NOT NULL COMMENT '详细地址',
  is_default     TINYINT      NOT NULL DEFAULT 0 COMMENT '1 默认地址',
  create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ---------------------------------------------------------------------
-- 6. 优惠券表
-- ---------------------------------------------------------------------
CREATE TABLE t_coupon (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(100)  NOT NULL COMMENT '优惠券名称',
  type            TINYINT       NOT NULL COMMENT '1 满减券 2 折扣券',
  threshold       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '使用门槛，0 表示无门槛',
  discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '满减金额（type=1 有效）',
  discount_rate   DECIMAL(3,2)  DEFAULT 1 COMMENT '折扣率 0.90=9 折（type=2 有效）',
  total           INT           NOT NULL COMMENT '发放总量',
  remain          INT           NOT NULL COMMENT '剩余数量',
  per_limit       INT           NOT NULL DEFAULT 1 COMMENT '每人限领张数',
  start_time      DATETIME      NOT NULL COMMENT '生效时间',
  end_time        DATETIME      NOT NULL COMMENT '失效时间',
  status          TINYINT       NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
  create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ---------------------------------------------------------------------
-- 7. 用户优惠券表
-- ---------------------------------------------------------------------
CREATE TABLE t_user_coupon (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT  NOT NULL,
  coupon_id    BIGINT  NOT NULL,
  status       TINYINT NOT NULL DEFAULT 0 COMMENT '0 未使用 1 已使用 2 已过期',
  order_id     BIGINT  COMMENT '使用的订单 ID',
  receive_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  use_time     DATETIME,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ---------------------------------------------------------------------
-- 8. 限时特价活动表
-- ---------------------------------------------------------------------
CREATE TABLE t_promotion (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(100)  NOT NULL COMMENT '活动标题',
  description VARCHAR(255)  COMMENT '活动说明',
  product_id  BIGINT        NOT NULL COMMENT '活动商品',
  promo_price DECIMAL(10,2) NOT NULL COMMENT '活动价',
  start_time  DATETIME      NOT NULL,
  end_time    DATETIME      NOT NULL,
  status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时特价活动表';

-- ---------------------------------------------------------------------
-- 9. 订单表
-- ---------------------------------------------------------------------
CREATE TABLE t_order (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no         VARCHAR(32)   NOT NULL UNIQUE COMMENT '订单号',
  user_id          BIGINT        NOT NULL,
  status           TINYINT       NOT NULL DEFAULT 0 COMMENT '0 待付款 1 待发货 2 待收货 3 已完成 4 已取消',
  total_amount     DECIMAL(10,2) NOT NULL COMMENT '商品总额',
  discount_amount  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  pay_amount       DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  user_coupon_id   BIGINT        COMMENT '使用的用户优惠券 ID',
  receiver_name    VARCHAR(50)   NOT NULL,
  receiver_phone   VARCHAR(20)   NOT NULL,
  receiver_address VARCHAR(300)  NOT NULL COMMENT '收货地址快照',
  remark           VARCHAR(200),
  pay_time         DATETIME,
  ship_time        DATETIME,
  finish_time      DATETIME,
  create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ---------------------------------------------------------------------
-- 10. 订单明细表
-- ---------------------------------------------------------------------
CREATE TABLE t_order_item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id      BIGINT        NOT NULL,
  product_id    BIGINT        NOT NULL,
  product_name  VARCHAR(100)  NOT NULL COMMENT '商品名称快照',
  product_image VARCHAR(255)  COMMENT '商品图片快照',
  price         DECIMAL(10,2) NOT NULL COMMENT '成交单价',
  quantity      INT           NOT NULL,
  total_price   DECIMAL(10,2) NOT NULL COMMENT '小计',
  INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 初始数据
-- =====================================================================

-- 账号：管理员 admin / 123456，测试用户 user / 123456（密码为 MD5）
INSERT INTO t_user(username, password, nickname, phone, role, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员',   '13800000000', 1, 1),
('user',  'e10adc3949ba59abbe56e057f20f883e', '测试用户', '13900000000', 0, 1),
('tom',   'e10adc3949ba59abbe56e057f20f883e', '汤姆',     '13700000000', 0, 1);

-- 商品分类
INSERT INTO t_category(id, name, icon, sort, status) VALUES
(1, '手机数码', 'https://img.icons8.com/color/96/smartphone-tablet.png', 1, 1),
(2, '电脑办公', 'https://img.icons8.com/color/96/laptop.png',            2, 1),
(3, '服装鞋帽', 'https://img.icons8.com/color/96/t-shirt.png',           3, 1),
(4, '食品生鲜', 'https://img.icons8.com/color/96/apple.png',             4, 1),
(5, '家居家电', 'https://img.icons8.com/color/96/sofa.png',              5, 1),
(6, '图书文娱', 'https://img.icons8.com/color/96/books.png',             6, 1);

-- 商品
INSERT INTO t_product(id, category_id, name, subtitle, main_image, detail, price, stock, sales, status) VALUES
(1, 1, '智能手机 Pro 12', '6.7 英寸 OLED 屏 / 5000mAh 大电池 / 骁龙旗舰芯片',
   'https://picsum.photos/seed/phone1/400/400', '旗舰级性能，全天候续航，专业影像系统，支持 120W 快充。', 3999.00, 100, 356, 1),
(2, 1, '无线蓝牙耳机', '主动降噪 / 30 小时续航 / 蓝牙 5.3',
   'https://picsum.photos/seed/earphone/400/400', '轻盈入耳设计，智能降噪，通话清晰。', 299.00, 500, 1280, 1),
(3, 1, '智能手表 S3', '血氧监测 / 心率监测 / 14 天长续航',
   'https://picsum.photos/seed/watch/400/400', '全天健康守护，100+ 运动模式，5ATM 防水。', 899.00, 200, 420, 1),
(4, 2, '轻薄笔记本电脑 14 英寸', '16GB 内存 / 512GB 固态 / 2.8K 高清屏',
   'https://picsum.photos/seed/laptop/400/400', '仅重 1.3kg，酷睿处理器，商务办公轻松应对。', 5499.00, 50, 88, 1),
(5, 2, '机械键盘 87 键', '青轴 / RGB 背光 / 有线',
   'https://picsum.photos/seed/keyboard/400/400', '段落感清脆，PBT 键帽耐磨，游戏办公两相宜。', 259.00, 300, 610, 1),
(6, 2, '无线鼠标', '静音按键 / 2.4G + 蓝牙双模 / 人体工学',
   'https://picsum.photos/seed/mouse/400/400', '贴合手型，长效续航，一键切换设备。', 79.00, 800, 2300, 1),
(7, 3, '男士纯棉短袖 T 恤', '100% 纯棉 / 多色可选 / 透气舒适',
   'https://picsum.photos/seed/tshirt/400/400', '精梳棉面料，柔软亲肤，简约百搭。', 69.00, 1000, 3200, 1),
(8, 3, '女士休闲运动鞋', '轻便透气 / 软底缓震 / 网面',
   'https://picsum.photos/seed/shoes/400/400', '飞织鞋面，回弹中底，久走不累脚。', 199.00, 400, 980, 1),
(9, 4, '进口车厘子 2kg 礼盒', 'JJ 级 / 空运直达 / 鲜甜多汁',
   'https://picsum.photos/seed/cherry/400/400', '智利车厘子，果径 28mm+，冷链配送。', 168.00, 150, 720, 1),
(10, 4, '有机牛奶 250ml×12 盒', '3.6g 优质乳蛋白 / 无添加',
   'https://picsum.photos/seed/milk/400/400', '有机牧场直供，营养醇香。', 59.90, 600, 1500, 1),
(11, 5, '智能扫地机器人', '激光导航 / 自动集尘 / 扫拖一体',
   'https://picsum.photos/seed/robot/400/400', '4000Pa 大吸力，APP 远程控制，解放双手。', 1999.00, 80, 156, 1),
(12, 5, '北欧简约台灯', '三档调光 / 护眼无蓝光 / USB 供电',
   'https://picsum.photos/seed/lamp/400/400', '柔和光线，简约设计，适合书桌卧室。', 89.00, 500, 430, 1),
(13, 6, '《Java 编程思想》（第 4 版）', '经典编程入门与进阶必读',
   'https://picsum.photos/seed/book1/400/400', 'Java 领域经典著作，全面讲解面向对象思想。', 108.00, 200, 860, 1),
(14, 6, '《Android 开发艺术探索》', 'Android 进阶经典',
   'https://picsum.photos/seed/book2/400/400', '深入 Android 系统机制与高级开发技巧。', 79.00, 150, 540, 1),
(15, 1, '经典功能手机（已下架示例）', '超长待机 / 大字大声',
   'https://picsum.photos/seed/oldphone/400/400', '老人机，简单易用。', 199.00, 0, 30, 0);

-- 优惠券（有效期覆盖当前时间前后一年，便于测试）
INSERT INTO t_coupon(id, name, type, threshold, discount_amount, discount_rate, total, remain, per_limit, start_time, end_time, status) VALUES
(1, '新人无门槛 10 元券',   1, 0.00,    10.00, 1.00, 1000, 1000, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY), 1),
(2, '满 200 减 30 券',      1, 200.00,  30.00, 1.00, 500,  500,  2, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY), 1),
(3, '满 1000 减 150 券',    1, 1000.00, 150.00, 1.00, 200, 200,  1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY), 1),
(4, '全场 9 折券',          2, 100.00,  0.00,  0.90, 300,  300,  1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY), 1),
(5, '已过期 8 折券（示例）', 2, 0.00,    0.00,  0.80, 100,  50,   1, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY),    1);

-- 限时特价活动（进行中）
INSERT INTO t_promotion(id, title, description, product_id, promo_price, start_time, end_time, status) VALUES
(1, '数码狂欢节', '蓝牙耳机限时直降 100 元', 2, 199.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(2, '开学季特惠', '轻薄本限时立减 500 元', 4, 4999.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(3, '鲜果周末价', '车厘子礼盒特价', 9, 128.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(4, '已结束的活动（示例）', '机械键盘特价', 5, 199.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1);

-- 测试用户的收货地址
INSERT INTO t_address(user_id, receiver_name, receiver_phone, province, city, district, detail, is_default) VALUES
(2, '张三', '13900000000', '广东省', '广州市', '天河区', '天河路 100 号 A 座 1001 室', 1),
(2, '张三', '13900000001', '广东省', '深圳市', '南山区', '科技园南区 8 栋 502', 0);

-- 测试用户已领取一张新人券
INSERT INTO t_user_coupon(user_id, coupon_id, status) VALUES (2, 1, 0);
UPDATE t_coupon SET remain = remain - 1 WHERE id = 1;

-- 测试用户购物车
INSERT INTO t_cart(user_id, product_id, quantity, checked) VALUES
(2, 2, 1, 1),
(2, 7, 2, 1);

-- 一笔已完成的历史订单（用于订单列表与销售统计演示）
INSERT INTO t_order(id, order_no, user_id, status, total_amount, discount_amount, pay_amount, user_coupon_id,
                    receiver_name, receiver_phone, receiver_address, remark, pay_time, ship_time, finish_time, create_time) VALUES
(1, '20260901120000123456', 2, 3, 358.00, 0.00, 358.00, NULL,
 '张三', '13900000000', '广东省广州市天河区天河路 100 号 A 座 1001 室', '请尽快发货',
 '2026-09-01 12:05:00', '2026-09-02 09:00:00', '2026-09-05 18:30:00', '2026-09-01 12:00:00');
INSERT INTO t_order_item(order_id, product_id, product_name, product_image, price, quantity, total_price) VALUES
(1, 5, '机械键盘 87 键', 'https://picsum.photos/seed/keyboard/400/400', 259.00, 1, 259.00),
(1, 6, '无线鼠标',       'https://picsum.photos/seed/mouse/400/400',    79.00,  1, 79.00),
(1, 12, '北欧简约台灯',  'https://picsum.photos/seed/lamp/400/400',     20.00,  1, 20.00);

-- 开放远程访问（手机 / 模拟器通过局域网连接时需要）。MySQL 8 语法：
-- CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
-- GRANT ALL ON shop_db.* TO 'root'@'%';
-- FLUSH PRIVILEGES;
