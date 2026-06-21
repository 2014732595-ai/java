-- ============================================
-- 二手线上交易系统 - 数据库初始化脚本
-- 数据库: secondhand_trade
-- 账号: root / 密码: 123456
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS secondhand_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE secondhand_trade;

-- ============================================
-- 表结构
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（可修改，3-20 位，字母、数字、下划线）',
    `password` VARCHAR(100) NOT NULL COMMENT '密码 (明文存储)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `phone_verified` TINYINT DEFAULT 0 COMMENT '手机号是否验证 (0-未验证 1-已验证)',
    `role` TINYINT DEFAULT 0 COMMENT '角色 (0-普通用户 1-管理员)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-禁用 1-正常)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT DEFAULT 0 COMMENT '排序(数字越小越靠前)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `condition_level` TINYINT DEFAULT NULL COMMENT '成色(1-全新 2-几乎全新 3-轻微使用 4-明显使用 5-功能正常)',
    `images` TEXT DEFAULT NULL COMMENT '图片URL(逗号分隔)',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-用户下架 1-在售 2-管理员下架)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待付款 1-已付款 2-已发货 3-已完成 4-已取消)',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 商品留言表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '留言用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '留言内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品留言表';

-- 收货地址表
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver` VARCHAR(50) NOT NULL COMMENT '收货人',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认(0-否 1-是)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ============================================
-- 初始化数据
-- ============================================

-- 测试用户 (密码：123456，明文存储，执行 change_v2.sql 后升级为 BCrypt 加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
('admin', '123456', '管理员', '13800000000', 1, 1),
('test1', '123456', '张三', '13800000001', 0, 1),
('test2', '123456', '李四', '13800000002', 0, 1);

-- 商品分类
INSERT INTO `category` (`name`, `sort`) VALUES
('手机数码', 1),
('电脑办公', 2),
('服饰鞋包', 3),
('图书音像', 4),
('家居日用', 5),
('运动户外', 6),
('母婴玩具', 7),
('其他', 8);

-- 测试商品
INSERT INTO `product` (`title`, `description`, `price`, `original_price`, `category_id`, `condition_level`, `seller_id`, `status`) VALUES
('iPhone 13 128G 白色', '去年购入，使用正常，无划痕，配件齐全', 3500.00, 5999.00, 1, 2, 2, 1),
('MacBook Pro 2021', 'M1芯片，16G内存，512G固态，电池循环50次', 7500.00, 14999.00, 2, 2, 2, 1),
('Java核心技术 卷I', '第11版，八成新，无笔记', 50.00, 119.00, 4, 3, 3, 1),
('Nike Air Jordan 1 42码', '全新未穿，朋友送的尺码不合适', 800.00, 1299.00, 3, 1, 3, 1),
('小米空气净化器', '使用一年，功能正常，附赠滤芯', 300.00, 699.00, 5, 3, 2, 1),
('迪卡侬山地自行车', '骑过几次，性能良好，送车锁', 500.00, 999.00, 6, 3, 3, 1);

-- 测试收货地址
INSERT INTO `address` (`user_id`, `receiver`, `phone`, `detail`, `is_default`) VALUES
(2, '张三', '13800000001', '北京市朝阳区XX街道XX小区1号楼101', 1),
(3, '李四', '13800000002', '上海市浦东新区XX路XX号2号楼202', 1);
