package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Product name is required")
    //@Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @jakarta.validation.constraints.Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 fractional digits")
    private BigDecimal price;

    @Column(nullable = false)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    private Size size;

    @Column(length = 255)
    @jakarta.validation.constraints.Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String imageUrl;

    // Many-to-One relationship with Category - EAGER fetch for Redis caching
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    @JsonBackReference // Back reference to avoid infinite loop
    private Category category;

    public enum Size {
        S, M, L, XL, XXL
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
