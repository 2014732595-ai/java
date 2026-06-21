package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.Product;

public interface FavoriteService {
    void addFavorite(Long userId, Long productId);
    void removeFavorite(Long userId, Long productId);
    IPage<Product> getFavoriteProducts(Long userId, int pageNum, int pageSize);
    boolean isFavorite(Long userId, Long productId);
}
