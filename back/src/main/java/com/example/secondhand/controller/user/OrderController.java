package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.dto.OrderDTO;
import com.example.secondhand.entity.Orders;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.OrderService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/orders")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Void> createOrder(@Validated @RequestBody OrderDTO orderDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        orderService.createOrder(user.getId(), orderDTO.getProductId(), orderDTO.getAddressId());
        return Result.success();
    }

    @GetMapping("/buy")
    public Result<IPage<Orders>> getBuyOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        IPage<Orders> page = orderService.getBuyOrders(user.getId(), pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/sell")
    public Result<IPage<Orders>> getSellOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        IPage<Orders> page = orderService.getSellOrders(user.getId(), pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        Integer status = params.get("status");
        orderService.updateOrderStatus(id, status, user.getId());
        return Result.success();
    }
}
