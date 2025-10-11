package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*")
public class CacheDebugController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Check if cache manager is working
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("cacheManagerType", cacheManager.getClass().getName());
        
        Collection<String> cacheNames = cacheManager.getCacheNames();
        status.put("cacheNames", cacheNames);
        
        Map<String, Object> cacheDetails = new HashMap<>();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cacheDetails.put(cacheName, cache.getClass().getName());
            }
        }
        status.put("cacheDetails", cacheDetails);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Check if a specific key exists in cache
     */
    @GetMapping("/check/{cacheName}/{key}")
    public ResponseEntity<Map<String, Object>> checkCacheKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        
        Map<String, Object> result = new HashMap<>();
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache == null) {
            result.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.ok(result);
        }
        
        Cache.ValueWrapper valueWrapper = cache.get(key);
        result.put("cacheName", cacheName);
        result.put("key", key);
        result.put("exists", valueWrapper != null);
        
        if (valueWrapper != null) {
            Object value = valueWrapper.get();
            result.put("valueType", value != null ? value.getClass().getName() : "null");
            result.put("valuePreview", value != null ? value.toString().substring(0, Math.min(200, value.toString().length())) : "null");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Clear a specific cache
     */
    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        Map<String, String> result = new HashMap<>();
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache == null) {
            result.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.ok(result);
        }
        
        cache.clear();
        result.put("message", "Cache '" + cacheName + "' cleared successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Clear all caches
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Map<String, Object> result = new HashMap<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();
        
        List<String> clearedCaches = new ArrayList<>();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                clearedCaches.add(cacheName);
            }
        }
        
        result.put("message", "All caches cleared");
        result.put("clearedCaches", clearedCaches);
        return ResponseEntity.ok(result);
    }

    /**
     * Get Redis connection info (if available)
     */
    @GetMapping("/redis-info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (redisTemplate != null) {
            try {
                // Try to ping Redis
                String ping = redisTemplate.getConnectionFactory().getConnection().ping();
                info.put("redisConnected", true);
                info.put("ping", ping);
                
                // Get all keys (be careful in production!)
                Set<String> keys = redisTemplate.keys("*");
                info.put("totalKeys", keys != null ? keys.size() : 0);
                info.put("keys", keys);
            } catch (Exception e) {
                info.put("redisConnected", false);
                info.put("error", e.getMessage());
            }
        } else {
            info.put("redisTemplate", "Not available");
        }
        
        return ResponseEntity.ok(info);
    }
}
