package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.dto.ProductDTO;
import com.example.secondhand.entity.Product;
import com.example.secondhand.mapper.ProductMapper;
import com.example.secondhand.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public IPage<Product> getProductPage(int pageNum, int pageSize, String keyword, Long categoryId) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    @Override
    @Transactional
    public void createProduct(Long sellerId, ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setSellerId(sellerId);
        product.setStatus(1);
        save(product);
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, Long sellerId, ProductDTO dto) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此商品");
        }
        BeanUtils.copyProperties(dto, product);
        updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权删除此商品");
        }
        product.setStatus(0);
        updateById(product);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long productId, Long sellerId, Integer status) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        product.setStatus(status);
        updateById(product);
    }

    @Override
    public IPage<Product> getMyProducts(Long sellerId, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId);
        wrapper.orderByDesc(Product::getCreateTime);
        return page(page, wrapper);
    }
}
