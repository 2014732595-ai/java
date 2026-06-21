package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Product;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.FavoriteService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/favorites")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Void> addFavorite(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId");
        Long userId = getCurrentUserId();
        favoriteService.addFavorite(userId, productId);
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, productId);
        return Result.success();
    }

    @GetMapping
    public Result<IPage<Product>> getFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        IPage<Product> page = favoriteService.getFavoriteProducts(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Boolean>> checkFavorite(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        boolean isFav = favoriteService.isFavorite(userId, productId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorite", isFav);
        return Result.success(result);
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
