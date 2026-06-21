package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.Refund;

public interface RefundService {
    void applyRefund(Long userId, Long orderId, String reason);
    IPage<Refund> getMyRefunds(Long userId, int pageNum, int pageSize);
    void cancelRefund(Long userId, Long refundId);
    IPage<Refund> getAllRefunds(int pageNum, int pageSize);
    void approveRefund(Long refundId, String adminRemark);
    void rejectRefund(Long refundId, String adminRemark);
}
