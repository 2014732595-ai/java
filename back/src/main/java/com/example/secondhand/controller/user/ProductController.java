package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.dto.ProductDTO;
import com.example.secondhand.entity.Product;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.ProductService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<IPage<Product>> listProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        IPage<Product> page = productService.getProductPage(pageNum, pageSize, keyword, categoryId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> create(@Validated @RequestBody ProductDTO productDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        productService.createProduct(user.getId(), productDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ProductDTO productDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        productService.updateProduct(id, user.getId(), productDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        productService.deleteProduct(id, user.getId());
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> params) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        Integer status = params.get("status");
        productService.toggleProductStatus(id, user.getId(), status);
        return Result.success();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<IPage<Product>> getMyProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        IPage<Product> page = productService.getMyProducts(user.getId(), pageNum, pageSize);
        return Result.success(page);
    }
}
