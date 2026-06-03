package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.Address;
import com.example.secondhand.entity.Orders;
import com.example.secondhand.entity.Product;
import com.example.secondhand.mapper.AddressMapper;
import com.example.secondhand.mapper.OrdersMapper;
import com.example.secondhand.mapper.ProductMapper;
import com.example.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Override
    @Transactional
    public Long createOrder(Long buyerId, Long productId, Long addressId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架或不可购买");
        }
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }

        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(productId);
        order.setAmount(product.getPrice());
        order.setStatus(0);
        order.setAddress(address.getReceiver() + " " + address.getPhone() + " " + address.getDetail());
        save(order);

        product.setStatus(0);
        productMapper.updateById(product);

        return order.getId();
    }

    @Override
    public IPage<Orders> getBuyOrders(Long buyerId, int pageNum, int pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getBuyerId, buyerId);
        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public IPage<Orders> getSellOrders(Long sellerId, int pageNum, int pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getSellerId, sellerId);
        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Integer status, Long userId) {
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        order.setStatus(status);
        updateById(order);
    }
}
