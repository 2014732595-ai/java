package com.example.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Product;
import com.example.secondhand.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Result<IPage<Product>> listProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<Product> page = adminService.getProductPage(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/status")
    public Result<Void> forceOfflineProduct(@PathVariable Long id) {
        adminService.forceOfflineProduct(id);
        return Result.success();
    }
}
