package com.example.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Refund;
import com.example.secondhand.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping
    public Result<IPage<Refund>> getAllRefunds(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Refund> page = refundService.getAllRefunds(pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approveRefund(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        String remark = params != null && params.get("remark") != null ? params.get("remark") : "";
        refundService.approveRefund(id, remark);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> rejectRefund(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        String remark = params != null && params.get("remark") != null ? params.get("remark") : "";
        refundService.rejectRefund(id, remark);
        return Result.success();
    }
}
