package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.Orders;
import com.example.secondhand.entity.Refund;
import com.example.secondhand.mapper.OrdersMapper;
import com.example.secondhand.mapper.RefundMapper;
import com.example.secondhand.service.NotificationService;
import com.example.secondhand.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public void applyRefund(Long userId, Long orderId, String reason) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        // 只有已付款或已发货的订单可以申请退款
        if (order.getStatus() < 1 || order.getStatus() > 2) {
            throw new BusinessException("当前订单状态不支持申请退款");
        }
        // 检查是否已有退款申请
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getOrderId, orderId);
        Refund existing = refundMapper.selectOne(wrapper);
        if (existing != null && existing.getStatus() != 2) {
            throw new BusinessException("该订单已有退款申请");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setReason(reason);
        refund.setAmount(order.getAmount());
        refund.setStatus(0);
        refundMapper.insert(refund);

        // 更新订单退款状态
        order.setRefundStatus(1);
        ordersMapper.updateById(order);

        // 通知买家：退款申请已提交
        notificationService.sendNotification(userId, "REFUND",
                "退款申请已提交", "您的退款申请已提交，请等待管理员处理", refund.getId());
    }

    @Override
    public IPage<Refund> getMyRefunds(Long userId, int pageNum, int pageSize) {
        Page<Refund> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getUserId, userId).orderByDesc(Refund::getCreateTime);
        return refundMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void cancelRefund(Long userId, Long refundId) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此退款");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException("当前退款状态不可取消");
        }
        refund.setStatus(2); // 标记为拒绝（用户自行取消）
        refundMapper.updateById(refund);

        // 恢复订单退款状态
        Orders order = ordersMapper.selectById(refund.getOrderId());
        if (order != null) {
            order.setRefundStatus(0);
            ordersMapper.updateById(order);
        }
    }

    @Override
    public IPage<Refund> getAllRefunds(int pageNum, int pageSize) {
        Page<Refund> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Refund::getCreateTime);
        return refundMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void approveRefund(Long refundId, String adminRemark) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException("当前退款状态不可操作");
        }
        refund.setStatus(1); // 已同意
        refund.setAdminRemark(adminRemark);
        refundMapper.updateById(refund);

        // 更新订单状态为已完成，退款状态为已退款
        Orders order = ordersMapper.selectById(refund.getOrderId());
        if (order != null) {
            order.setStatus(3);
            order.setRefundStatus(2);
            ordersMapper.updateById(order);
        }

        // 通知买家：退款已同意
        notificationService.sendNotification(refund.getUserId(), "REFUND",
                "退款已同意", "您的退款申请已通过，退款金额：￥" + refund.getAmount(), refund.getId());
    }

    @Override
    @Transactional
    public void rejectRefund(Long refundId, String adminRemark) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException("当前退款状态不可操作");
        }
        refund.setStatus(2); // 已拒绝
        refund.setAdminRemark(adminRemark);
        refundMapper.updateById(refund);

        // 恢复订单退款状态
        Orders order = ordersMapper.selectById(refund.getOrderId());
        if (order != null) {
            order.setRefundStatus(3);
            ordersMapper.updateById(order);
        }

        // 通知买家：退款已拒绝
        notificationService.sendNotification(refund.getUserId(), "REFUND",
                "退款已拒绝", "您的退款申请已被拒绝" + (adminRemark != null && !adminRemark.isEmpty() ? "，原因：" + adminRemark : ""), refund.getId());
    }
}
