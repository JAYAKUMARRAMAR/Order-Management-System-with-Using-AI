package com.jayakumar.product.service;

import com.jayakumar.product.dto.ProductRequest;
import com.jayakumar.product.dto.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(
            Long id,
            ProductRequest request
    );

    void deleteProduct(Long id);

    Page<ProductResponse> searchProducts(
            String name,
            int page,
            int size,
            String sortBy,
            String direction
    );
}