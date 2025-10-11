package com.example.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /*
     * ========================================
     * CACHE NAMES USED IN THE APPLICATION
     * ========================================
     * 
     * 1. "products" - Product entity cache
     *    Keys: productId, 'all'
     * 
     * 2. "categories" - Category entity cache
     *    Keys: categoryId, 'all', 'allWithProducts', 'name:{name}', 'withProducts:{id}'
     * 
     * 3. "users" - User entity cache
     *    Keys: userId, 'all', 'username:{username}', 'email:{email}'
     * 
     * 4. "reviews" - Review entity cache
     *    Keys: reviewId, 'all', 'product:{productId}', 'user:{userId}'
     * 
     * 5. "addresses" - Address entity cache
     *    Keys: addressId, 'user:{userId}', 'default:{userId}', 'user:{userId}:type:{type}'
     * 
     * Note: Orders, Payments, ShoppingCart, and Wishlist are NOT cached
     * due to their transactional nature and frequent modifications.
     */

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Fail on empty beans to catch serialization issues early
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Enable default typing for polymorphic deserialization
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        mapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        System.out.println("✅ Creating RedisCacheManager with custom serialization...");
        
        // Configure RedisCacheConfiguration with custom JSON serialization
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(createRedisObjectMapper())
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
    
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                System.err.println("❌ Cache GET error for key: " + key + " in cache: " + cache.getName());
                exception.printStackTrace();
                super.handleCacheGetError(exception, cache, key);
            }
            
            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                System.err.println("❌ Cache PUT error for key: " + key + " in cache: " + cache.getName());
                exception.printStackTrace();
                super.handleCachePutError(exception, cache, key, value);
            }
        };
    }
}
