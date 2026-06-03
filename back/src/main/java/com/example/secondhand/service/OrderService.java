package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand.entity.Orders;

public interface OrderService extends IService<Orders> {

    Long createOrder(Long buyerId, Long productId, Long addressId);

    IPage<Orders> getBuyOrders(Long buyerId, int pageNum, int pageSize);

    IPage<Orders> getSellOrders(Long sellerId, int pageNum, int pageSize);

    void updateOrderStatus(Long orderId, Integer status, Long userId);
}
