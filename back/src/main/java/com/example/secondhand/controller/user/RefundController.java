package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Refund;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.RefundService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/refunds")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Void> applyRefund(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        refundService.applyRefund(userId, orderId, reason);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<IPage<Refund>> getMyRefunds(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        IPage<Refund> page = refundService.getMyRefunds(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelRefund(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        refundService.cancelRefund(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof Long) {
            return (Long) credentials;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        return user.getId();
    }
}
