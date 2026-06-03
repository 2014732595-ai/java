package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.Orders;
import com.example.secondhand.entity.Product;
import com.example.secondhand.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public interface AdminService {

    IPage<User> getUserPage(String keyword, int pageNum, int pageSize);

    void updateUserStatus(Long userId, Integer status);

    IPage<Product> getProductPage(String keyword, int pageNum, int pageSize);

    void forceOfflineProduct(Long productId);

    IPage<Orders> getOrderPage(String keyword, int pageNum, int pageSize);

    Map<String, Object> getStats();

    void deleteComment(Long commentId);
}
