-- ============================================
-- 二手线上交易系统 V2 - 数据库升级脚本
-- 在 init.sql 基础上执行
-- ============================================

USE secondhand_trade;

-- ============================================
-- 1. 收藏表
-- ============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- ============================================
-- 2. 评价表
-- ============================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '评价用户ID',
    `rating` TINYINT NOT NULL COMMENT '评分(1-5星)',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `images` TEXT DEFAULT NULL COMMENT '评价图片(逗号分隔)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ============================================
-- 3. 聊天消息表
-- ============================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `from_id` BIGINT NOT NULL COMMENT '发送者ID',
    `to_id` BIGINT NOT NULL COMMENT '接收者ID',
    `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读(0-未读 1-已读)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_id` (`from_id`),
    KEY `idx_to_id` (`to_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ============================================
-- 4. 退款表
-- ============================================
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待处理 1-已同意 2-已拒绝 3-已退款)',
    `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

-- ============================================
-- 5. 通知表
-- ============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型(ORDER/REFUND/CHAT/SYSTEM)',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读(0-未读 1-已读)',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- ============================================
-- 6. 商品表新增字段
-- ============================================
ALTER TABLE `product` ADD COLUMN `avg_rating` DECIMAL(2,1) DEFAULT 0.0 COMMENT '平均评分' AFTER `status`;
ALTER TABLE `product` ADD COLUMN `review_count` INT DEFAULT 0 COMMENT '评价数量' AFTER `avg_rating`;

-- ============================================
-- 7. 订单表新增字段
-- ============================================
ALTER TABLE `orders` ADD COLUMN `refund_status` TINYINT DEFAULT 0 COMMENT '退款状态(0-无退款 1-退款中 2-已退款 3-退款被拒)' AFTER `status`;

-- ============================================
-- 8. 用户表密码更新为 BCrypt 哈希（密码：123456）
-- 无论当前是明文还是旧哈希，统一更新为正确的 BCrypt 哈希
-- ============================================
UPDATE `user` SET `password` = '$2a$10$oe/uUCWUiKVZ26K5uGXZZeCShrXtxnSTI.4/uWKtfL6TXu4WHX/4m' WHERE `username` IN ('admin', 'test1', 'test2');
