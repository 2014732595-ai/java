package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.Favorite;
import com.example.secondhand.entity.Product;
import com.example.secondhand.mapper.FavoriteMapper;
import com.example.secondhand.mapper.ProductMapper;
import com.example.secondhand.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 检查是否已收藏
        if (isFavorite(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public IPage<Product> getFavoriteProducts(Long userId, int pageNum, int pageSize) {
        // 先查询收藏记录
        Page<Favorite> favPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        IPage<Favorite> favResult = favoriteMapper.selectPage(favPage, wrapper);

        // 转换为商品列表
        List<Long> productIds = favResult.getRecords().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, favResult.getTotal());
        }

        List<Product> products = productMapper.selectBatchIds(productIds);
        Page<Product> productPage = new Page<>(pageNum, pageSize, favResult.getTotal());
        productPage.setRecords(products);
        return productPage;
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
