package com.example.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Review;
import com.example.secondhand.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public Result<IPage<Review>> getAllReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Review> page = reviewService.getAllReviews(pageNum, pageSize);
        return Result.success(page);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success();
    }
}
