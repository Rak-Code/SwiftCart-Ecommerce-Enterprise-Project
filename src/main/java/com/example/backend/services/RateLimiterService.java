package com.example.backend.services;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing rate limiting using Bucket4j.
 * Provides per-user and per-endpoint rate limiting capabilities.
 */
@Service
public class RateLimiterService {

    // Separate buckets for different endpoint types
    private final Map<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> paymentBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> adminBuckets = new ConcurrentHashMap<>();
    // Product-specific buckets
    private final Map<String, Bucket> productGeneralBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> productSearchBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> productFilterBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> productCacheBuckets = new ConcurrentHashMap<>();

    /**
     * Resolves bucket for login endpoints (stricter limits)
     * Limit: 5 requests per minute
     */
    public Bucket resolveLoginBucket(String key) {
        return loginBuckets.computeIfAbsent(key, k -> createLoginBucket());
    }

    /**
     * Resolves bucket for registration endpoints (stricter limits)
     * Limit: 3 requests per minute
     */
    public Bucket resolveRegisterBucket(String key) {
        return registerBuckets.computeIfAbsent(key, k -> createRegisterBucket());
    }

    /**
     * Resolves bucket for payment endpoints (moderate limits)
     * Limit: 20 requests per minute
     */
    public Bucket resolvePaymentBucket(String key) {
        return paymentBuckets.computeIfAbsent(key, k -> createPaymentBucket());
    }

    /**
     * Resolves bucket for admin endpoints (higher limits)
     * Limit: 200 requests per minute
     */
    public Bucket resolveAdminBucket(String key) {
        return adminBuckets.computeIfAbsent(key, k -> createAdminBucket());
    }

    /**
     * Resolves default bucket for general endpoints
     * Limit: 100 requests per minute
     */
    public Bucket resolveDefaultBucket(String key) {
        return defaultBuckets.computeIfAbsent(key, k -> createDefaultBucket());
    }

    /**
     * Resolves bucket for general product browsing (GET /api/products, /api/products/{id}, /category/{id})
     * Limit: 60 requests per minute
     */
    public Bucket resolveProductGeneralBucket(String key) {
        return productGeneralBuckets.computeIfAbsent(key, k -> createProductGeneralBucket());
    }

    /**
     * Resolves bucket for product search endpoint (GET /api/products/search/{name})
     * Limit: 20 requests per minute
     */
    public Bucket resolveProductSearchBucket(String key) {
        return productSearchBuckets.computeIfAbsent(key, k -> createProductSearchBucket());
    }

    /**
     * Resolves bucket for product filter endpoint (GET /api/products/filter)
     * Limit: 30 requests per minute
     */
    public Bucket resolveProductFilterBucket(String key) {
        return productFilterBuckets.computeIfAbsent(key, k -> createProductFilterBucket());
    }

    /**
     * Resolves bucket for products cache refresh (POST /api/products/refresh-products-cache)
     * Limit: 2 requests per minute
     */
    public Bucket resolveProductCacheBucket(String key) {
        return productCacheBuckets.computeIfAbsent(key, k -> createProductCacheBucket());
    }

    /**
     * Creates a bucket for login endpoints
     * 5 requests per minute with greedy refill
     */
    private Bucket createLoginBucket() {
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for registration endpoints
     * 3 requests per minute with greedy refill
     */
    private Bucket createRegisterBucket() {
        Refill refill = Refill.greedy(3, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(3, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for payment endpoints
     * 20 requests per minute with greedy refill
     */
    private Bucket createPaymentBucket() {
        Refill refill = Refill.greedy(20, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(20, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for admin endpoints
     * 200 requests per minute with greedy refill
     */
    private Bucket createAdminBucket() {
        Refill refill = Refill.greedy(200, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(200, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a default bucket for general endpoints
     * 100 requests per minute with greedy refill
     */
    private Bucket createDefaultBucket() {
        Refill refill = Refill.greedy(100, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(100, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for general product browsing
     * 60 requests per minute
     */
    private Bucket createProductGeneralBucket() {
        Refill refill = Refill.greedy(60, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(60, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for product search
     * 20 requests per minute
     */
    private Bucket createProductSearchBucket() {
        Refill refill = Refill.greedy(20, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(20, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for product filter
     * 30 requests per minute
     */
    private Bucket createProductFilterBucket() {
        Refill refill = Refill.greedy(30, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(30, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for products cache refresh
     * 2 requests per minute
     */
    private Bucket createProductCacheBucket() {
        Refill refill = Refill.greedy(2, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(2, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Clears all buckets for a specific user key
     * Useful for testing or manual reset
     */
    public void clearBucketsForUser(String key) {
        defaultBuckets.remove(key);
        loginBuckets.remove(key);
        registerBuckets.remove(key);
        paymentBuckets.remove(key);
        adminBuckets.remove(key);
        productGeneralBuckets.remove(key);
        productSearchBuckets.remove(key);
        productFilterBuckets.remove(key);
        productCacheBuckets.remove(key);
    }

    /**
     * Clears all buckets
     * Useful for testing or system reset
     */
    public void clearAllBuckets() {
        defaultBuckets.clear();
        loginBuckets.clear();
        registerBuckets.clear();
        paymentBuckets.clear();
        adminBuckets.clear();
        productGeneralBuckets.clear();
        productSearchBuckets.clear();
        productFilterBuckets.clear();
        productCacheBuckets.clear();
    }
}
