package com.example.backend.services;

import com.example.backend.model.Review;
import com.example.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // ---------------- Create ----------------
    @CachePut(value = "reviews", key = "#result.reviewId")
    @Caching(evict = {
            @CacheEvict(value = "reviews", key = "'all'"),
            @CacheEvict(value = "reviews", key = "'product:' + #result.product.productId"),
            @CacheEvict(value = "reviews", key = "'user:' + #result.user.userId")
    })
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    // ---------------- Read All ----------------
    @Cacheable(value = "reviews", key = "'all'")
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // ---------------- Read By ID ----------------
    @Cacheable(value = "reviews", key = "#reviewId")
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    // ---------------- Update ----------------
    @Caching(
            put = { @CachePut(value = "reviews", key = "#reviewId") },
            evict = { 
                @CacheEvict(value = "reviews", key = "'all'"),
                @CacheEvict(value = "reviews", key = "'product:' + #result.product.productId"),
                @CacheEvict(value = "reviews", key = "'user:' + #result.user.userId")
            }
    )
    public Review updateReview(Long reviewId, Review reviewDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());
        return reviewRepository.save(review);
    }

    // ---------------- Delete ----------------
    @Caching(evict = {
            @CacheEvict(value = "reviews", key = "#reviewId"),
            @CacheEvict(value = "reviews", key = "'all'"),
            @CacheEvict(value = "reviews", allEntries = true) // Clear all to ensure product/user caches are cleared
    })
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    // ---------------- Get Reviews by Product ----------------
    @Cacheable(value = "reviews", key = "'product:' + #productId")
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProduct_ProductId(productId);
    }

    // ---------------- Get Reviews by User ----------------
    @Cacheable(value = "reviews", key = "'user:' + #userId")
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUser_UserId(userId);
    }
}