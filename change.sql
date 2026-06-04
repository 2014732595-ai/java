-- ============================================
-- 二手交易系统 - 数据库变更脚本
-- 说明：所有数据库 CRUD 操作都在此文件中进行
-- ============================================

-- ============================================
-- 1. 用户相关操作
-- ============================================

-- 1.1 查询用户信息
-- SELECT * FROM user WHERE phone = '13800138000';

-- 1.2 更新用户名
-- UPDATE user SET username = '新用户' WHERE id = 1;

-- 1.3 更新密码
-- UPDATE user SET password = 'newpassword123' WHERE phone = '13800138000';

-- 1.4 插入新用户（手机号注册）
-- INSERT INTO user (phone, password, role, create_time) 
-- VALUES ('13800138000', '', 1, NOW());

-- ============================================
-- 2. 验证码相关操作
-- ============================================

-- 2.1 插入验证码
-- INSERT INTO verification_code (phone, code, expire_time, create_time) 
-- VALUES ('13800138000', '123456', DATE_ADD(NOW(), INTERVAL 5 MINUTE), NOW());

-- 2.2 查询验证码
-- SELECT code FROM verification_code WHERE phone = '13800138000' ORDER BY id DESC LIMIT 1;

-- 2.3 删除过期验证码
-- DELETE FROM verification_code WHERE expire_time < NOW();

-- ============================================
-- 3. 管理员相关操作
-- ============================================

-- 3.1 查询管理员信息
-- SELECT * FROM admin WHERE username = 'admin';

-- 3.2 更新管理员密码
-- UPDATE admin SET password = 'newadminpass' WHERE username = 'admin';

-- ============================================
-- 4. 商品相关操作
-- ============================================

-- 4.1 查询商品列表
-- SELECT * FROM product WHERE status = 1 ORDER BY create_time DESC;

-- 4.2 查询商品详情
-- SELECT * FROM product WHERE id = 1;

-- 4.3 插入新商品
-- INSERT INTO product (title, description, price, original_price, category_id, condition_level, seller_id, status, create_time) 
-- VALUES ('商品名称', '商品描述', 99.00, 199.00, 1, 2, 1, 1, NOW());

-- 4.4 更新商品信息
-- UPDATE product SET title = '新商品名称', price = 199.00 WHERE id = 1;

-- 4.5 删除商品（逻辑删除）
-- UPDATE product SET status = 0 WHERE id = 1;

-- ============================================
-- 5. 分类相关操作
-- ============================================

-- 5.1 查询所有分类
-- SELECT * FROM category ORDER BY sort;

-- 5.2 插入新分类
-- INSERT INTO category (name, sort) VALUES ('新分类', 1);

-- ============================================
-- 6. 订单相关操作
-- ============================================

-- 6.1 查询订单列表
-- SELECT * FROM orders WHERE buyer_id = 1 ORDER BY create_time DESC;

-- 6.2 查询订单详情
-- SELECT * FROM orders WHERE id = 1;

-- 6.3 插入新订单
-- INSERT INTO orders (order_no, buyer_id, seller_id, product_id, amount, status, create_time) 
-- VALUES ('ORD202401010001', 2, 1, 1, 99.00, 1, NOW());

-- 6.4 更新订单状态
-- UPDATE orders SET status = 2 WHERE id = 1;

-- ============================================
-- 7. 收藏相关操作
-- ============================================

-- 7.1 查询用户收藏列表
-- SELECT * FROM favorite WHERE user_id = 1 ORDER BY create_time DESC;

-- 7.2 添加收藏
-- INSERT INTO favorite (user_id, goods_id, create_time) VALUES (1, 5, NOW());

-- 7.3 取消收藏
-- DELETE FROM favorite WHERE user_id = 1 AND goods_id = 5;

-- ============================================
-- 8. 聊天记录相关操作
-- ============================================

-- 8.1 查询聊天消息
-- SELECT * FROM chat_message WHERE (sender_id = 1 AND receiver_id = 2) 
-- OR (sender_id = 2 AND receiver_id = 1) ORDER BY create_time;

-- 8.2 发送消息
-- INSERT INTO chat_message (sender_id, receiver_id, content, create_time) 
-- VALUES (1, 2, '你好', NOW());

-- ============================================
-- 9. 统计数据相关操作
-- ============================================

-- 9.1 查询用户发布的商品数量
-- SELECT COUNT(*) FROM product WHERE seller_id = 1;

-- 9.2 查询用户收藏的商品数量
-- SELECT COUNT(*) FROM favorite WHERE user_id = 1;

-- 9.3 查询用户买入的订单数量
-- SELECT COUNT(*) FROM orders WHERE buyer_id = 1;

-- 9.4 查询用户卖出的订单数量
-- SELECT COUNT(*) FROM orders WHERE seller_id = 1;

-- ============================================
-- 10. 批量数据插入
-- ============================================

-- 10.1 新增 5 个普通用户（role=0，密码：1008611）
-- 如果用户已存在则跳过
INSERT IGNORE INTO user (username, password, nickname, phone, role, status, create_time) VALUES
('user001', '1008611', '张三', '13800138001', 0, 1, NOW()),
('user002', '1008611', '李四', '13800138002', 0, 1, NOW()),
('user003', '1008611', '王五', '13800138003', 0, 1, NOW()),
('user004', '1008611', '赵六', '13800138004', 0, 1, NOW()),
('user005', '1008611', '孙七', '13800138005', 0, 1, NOW());

-- 10.2 新增 30 个商品
INSERT INTO product (title, description, price, original_price, category_id, condition_level, seller_id, status, create_time) VALUES
('iPhone 13 二手手机', '95 新，无划痕，电池健康度 90%', 3500.00, 5999.00, 1, 2, 4, 1, NOW()),
('MacBook Pro 2020', '使用一年，轻微使用痕迹，性能完好', 6500.00, 12999.00, 2, 2, 4, 1, NOW()),
('AirPods Pro', '仅拆封，几乎未使用', 1200.00, 1999.00, 1, 1, 4, 1, NOW()),
('iPad Air 4', '64G WiFi 版，带保护套', 2800.00, 4799.00, 2, 2, 5, 1, NOW()),
('Sony 索尼 WH-1000XM4 耳机', '降噪耳机，9 成新', 1500.00, 2499.00, 1, 3, 5, 1, NOW()),
('Nintendo Switch 游戏机', '国行版，带 3 个游戏卡带', 1800.00, 2599.00, 7, 2, 6, 1, NOW()),
('PS5 游戏主机', '光驱版，手柄两个', 3800.00, 4299.00, 7, 2, 6, 1, NOW()),
('Canon 佳能 EOS R6 相机', '微单相机，99 新，配件齐全', 12000.00, 16999.00, 8, 1, 7, 1, NOW()),
('DJI 大疆 Mavic Air 2 无人机', '畅飞套装，飞行次数少', 4500.00, 7899.00, 8, 2, 7, 1, NOW()),
('GoPro Hero 9 运动相机', '全套配件，使用次数少', 2000.00, 3499.00, 8, 2, 8, 1, NOW()),
('Herman Miller 人体工学椅', 'Aeron 型号，二手 9 成新', 3500.00, 8999.00, 5, 3, 8, 1, NOW()),
('IKEA 书桌', '简约风格，120x60cm', 300.00, 599.00, 5, 3, 8, 1, NOW()),
('小米台灯 1S', '智能台灯，未拆封', 80.00, 169.00, 5, 1, 9, 1, NOW()),
('机械键盘 Keychron K2', '红轴，RGB 背光', 350.00, 599.00, 2, 2, 9, 1, NOW()),
('罗技 MX Master 3 鼠标', '无线鼠标，办公神器', 450.00, 799.00, 2, 2, 9, 1, NOW()),
('戴尔 27 寸 4K 显示器', 'U2720Q 型号，色彩准确', 2200.00, 3999.00, 2, 2, 10, 1, NOW()),
('西数 1TB 移动硬盘', 'USB3.0，轻薄便携', 300.00, 599.00, 2, 2, 10, 1, NOW()),
('Kindle Paperwhite 4', '8G 版，带保护套', 600.00, 1299.00, 4, 2, 10, 1, NOW()),
('得到阅读器', '7.8 寸墨水屏，全新未拆', 1500.00, 2599.00, 4, 1, 11, 1, NOW()),
('乐高 星球大战系列', '75331 曼达洛人，未拆封', 800.00, 1299.00, 7, 1, 11, 1, NOW()),
('泡泡玛特 盲盒套装', '5 个一套，未拆', 300.00, 599.00, 7, 1, 11, 1, NOW()),
('Nike Air Jordan 1', '42 码，9 成新，有鞋盒', 1200.00, 1999.00, 3, 3, 12, 1, NOW()),
('Adidas 运动外套', 'L 码，速干材质', 200.00, 499.00, 3, 2, 12, 1, NOW()),
('优衣库 羽绒服', 'M 码，90% 白鸭绒', 350.00, 799.00, 3, 2, 12, 1, NOW()),
('无印良品 双肩包', '防水材质，电脑仓', 180.00, 399.00, 3, 2, 13, 1, NOW()),
('小米手环 6', 'NFC 版，9 成新', 150.00, 299.00, 1, 3, 13, 1, NOW()),
('Apple Watch SE', '44mm GPS 版，运动表带', 1500.00, 2499.00, 1, 2, 13, 1, NOW()),
('Fitbit Charge 5', '健康追踪器，全新', 800.00, 1299.00, 1, 1, 14, 1, NOW()),
('露营帐篷 3-4 人', '双层防雨，带防潮垫', 400.00, 899.00, 6, 2, 14, 1, NOW()),
('折叠自行车', '20 寸，7 速变速，9 成新', 600.00, 1299.00, 6, 3, 14, 1, NOW());
