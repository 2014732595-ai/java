package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand.dto.ProductDTO;
import com.example.secondhand.entity.Product;

public interface ProductService extends IService<Product> {

    IPage<Product> getProductPage(int pageNum, int pageSize, String keyword, Long categoryId);

    Product getProductDetail(Long id);

    void createProduct(Long sellerId, ProductDTO dto);

    void updateProduct(Long productId, Long sellerId, ProductDTO dto);

    void deleteProduct(Long productId, Long sellerId);

    void toggleProductStatus(Long productId, Long sellerId, Integer status);

    IPage<Product> getMyProducts(Long sellerId, int pageNum, int pageSize);
}
