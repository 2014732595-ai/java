package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.Review;

public interface ReviewService {
    void createReview(Long userId, Long orderId, Long productId, Integer rating, String content, String images);
    IPage<Review> getProductReviews(Long productId, int pageNum, int pageSize);
    IPage<Review> getMyReviews(Long userId, int pageNum, int pageSize);
    IPage<Review> getAllReviews(int pageNum, int pageSize);
    void deleteReview(Long reviewId);
    boolean hasReviewed(Long orderId);
}
