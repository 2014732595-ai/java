package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.Orders;
import com.example.secondhand.entity.Product;
import com.example.secondhand.entity.Review;
import com.example.secondhand.mapper.OrdersMapper;
import com.example.secondhand.mapper.ProductMapper;
import com.example.secondhand.mapper.ReviewMapper;
import com.example.secondhand.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public void createReview(Long userId, Long orderId, Long productId, Integer rating, String content, String images) {
        // 校验订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("订单未完成，不能评价");
        }
        // 校验是否已评价
        if (hasReviewed(orderId)) {
            throw new BusinessException("该订单已评价");
        }
        // 校验评分
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在 1-5 之间");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);

        // 更新商品评分统计
        updateProductRating(productId);
    }

    @Override
    public IPage<Review> getProductReviews(Long productId, int pageNum, int pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId).orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Review> getMyReviews(Long userId, int pageNum, int pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId).orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Review> getAllReviews(int pageNum, int pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(reviewId);
        // 更新商品评分统计
        updateProductRating(review.getProductId());
    }

    @Override
    public boolean hasReviewed(Long orderId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        return reviewMapper.selectCount(wrapper) > 0;
    }

    private void updateProductRating(Long productId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        long count = reviewMapper.selectCount(wrapper);

        Product product = productMapper.selectById(productId);
        if (product == null) return;

        if (count == 0) {
            product.setAvgRating(new BigDecimal("0.0"));
            product.setReviewCount(0);
        } else {
            // 计算平均分
            LambdaQueryWrapper<Review> allWrapper = new LambdaQueryWrapper<>();
            allWrapper.eq(Review::getProductId, productId);
            double avgRating = reviewMapper.selectList(allWrapper).stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            product.setAvgRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
            product.setReviewCount((int) count);
        }
        productMapper.updateById(product);
    }
}
