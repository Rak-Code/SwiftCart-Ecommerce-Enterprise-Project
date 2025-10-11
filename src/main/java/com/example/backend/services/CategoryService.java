package com.example.backend.services;

import com.example.backend.model.Category;
import com.example.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // ---------------- Create ----------------
    @CachePut(value = "categories", key = "#result.categoryId")
    @CacheEvict(value = "categories", key = "'all'")
    public Category createCategory(@Valid Category category) {
        return categoryRepository.save(category);
    }

    // ---------------- Read All ----------------
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ---------------- Read By ID ----------------
    @Cacheable(value = "categories", key = "#categoryId")
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    // ---------------- Update ----------------
    @Caching(
            put = { @CachePut(value = "categories", key = "#categoryId") },
            evict = { 
                @CacheEvict(value = "categories", key = "'all'"),
                @CacheEvict(value = "categories", key = "'allWithProducts'")
            }
    )
    public Category updateCategory(Long categoryId, @Valid Category categoryDetails) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }

    // ---------------- Delete ----------------
    @Caching(evict = {
            @CacheEvict(value = "categories", key = "#categoryId"),
            @CacheEvict(value = "categories", key = "'all'"),
            @CacheEvict(value = "categories", key = "'allWithProducts'"),
            @CacheEvict(value = "products", key = "'all'") // Evict products cache as category affects product listings
    })
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    // ---------------- Find by Name ----------------
    @Cacheable(value = "categories", key = "'name:' + #name.toLowerCase()")
    public Optional<Category> findByNameIgnoreCase(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    // Find categories by name containing a string (case-insensitive)
    // Note: Search queries are not cached as they can have many variations
    public List<Category> findByNameContainingIgnoreCase(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    // ---------------- Fetch with Products ----------------
    @Cacheable(value = "categories", key = "'withProducts:' + #categoryId")
    public Optional<Category> findByIdWithProducts(Long categoryId) {
        return categoryRepository.findByIdWithProducts(categoryId);
    }

    // Fetch all categories with products
    @Cacheable(value = "categories", key = "'allWithProducts'")
    public List<Category> findAllWithProducts() {
        return categoryRepository.findAllWithProducts();
    }
}
