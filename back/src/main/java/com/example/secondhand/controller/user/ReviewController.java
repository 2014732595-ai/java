package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Review;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.ReviewService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> createReview(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer rating = Integer.valueOf(params.get("rating").toString());
        String content = params.get("content") != null ? params.get("content").toString() : null;
        String images = params.get("images") != null ? params.get("images").toString() : null;
        reviewService.createReview(userId, orderId, productId, rating, content, images);
        return Result.success();
    }

    @GetMapping("/product/{productId}")
    public Result<IPage<Review>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Review> page = reviewService.getProductReviews(productId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<IPage<Review>> getMyReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        IPage<Review> page = reviewService.getMyReviews(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/check/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Map<String, Boolean>> hasReviewed(@PathVariable Long orderId) {
        boolean reviewed = reviewService.hasReviewed(orderId);
        HashMap<String, Boolean> result = new HashMap<>();
        result.put("reviewed", reviewed);
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
