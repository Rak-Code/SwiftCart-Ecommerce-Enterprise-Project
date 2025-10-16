package com.example.backend.services;

import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "products")
public class ProductService {
	
	@Autowired
    private CacheManager cacheManager;


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistService wishlistService;
    
    public void evictAllProductsCache() {
        if (cacheManager.getCache("products") != null) {
            cacheManager.getCache("products").evict("all");
        }
        // Also clear category-specific caches to avoid returning stale product objects
        if (cacheManager.getCache("categoryProducts") != null) {
            cacheManager.getCache("categoryProducts").clear();
        }
    }


    // ---------------- Create ----------------
    @CachePut(value = "products", key = "#result.productId")
    @Caching(evict = {
            @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "categories", key = "'allWithProducts'"), // Evict category cache with products
        @CacheEvict(value = "categoryProducts", allEntries = true) // Clear category-specific product lists
    })
    public Product createProduct(@Valid Product product) {
        return productRepository.save(product);
    }

    // ---------------- Read All ----------------
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ---------------- Read By ID ----------------
    @Cacheable(value = "products", key = "#productId")
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    // ---------------- Update ----------------
    @Caching(
            put = { @CachePut(value = "products", key = "#productId") },
            evict = { 
                @CacheEvict(value = "products", key = "'all'"),
                @CacheEvict(value = "categories", key = "'allWithProducts'"), // Evict category cache with products
                @CacheEvict(value = "categoryProducts", allEntries = true) // Clear category-specific product lists
            }
    )
    public Product updateProduct(Long productId, @Valid Product productDetails) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setSize(productDetails.getSize());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());

        return productRepository.save(product);
    }

    // ---------------- Delete ----------------
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "categories", key = "'allWithProducts'"), // Evict category cache with products
        @CacheEvict(value = "categoryProducts", allEntries = true), // Clear category-specific product lists
            @CacheEvict(value = "reviews", allEntries = true) // Clear reviews as product is deleted
    })
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        wishlistService.removeByProductId(productId);
        productRepository.deleteById(productId);
    }



    @Cacheable(value = "categoryProducts", key = "#categoryId")
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    // ---------------- Search/Filter (Cached) ----------------
    // Search is not cached due to many variations - consider implementing search-specific caching strategy
    public List<Product> findByNameContainingIgnoreCase(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Filter with multiple parameters - cached with composite key
    @Cacheable(value = "products", key = "'filter:' + #categoryId + ':' + #minPrice + ':' + #maxPrice + ':' + #size + ':' + #inStock")
    public List<Product> filterProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Product.Size size, Boolean inStock) {
        return productRepository.findByFilters(categoryId, minPrice, maxPrice, size, inStock);
    }


	
}
