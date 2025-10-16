package com.example.backend.security;

import com.example.backend.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter that applies different rate limits based on endpoint type.
 * Integrates with Spring Security to identify authenticated users.
 */
@Component
@Order(1)
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        // Skip rate limiting for Swagger/OpenAPI endpoints
        if (requestURI.startsWith("/v3/api-docs") || 
            requestURI.startsWith("/swagger-ui") || 
            requestURI.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Disable rate limiting for Products module
        if (requestURI.startsWith("/api/products")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract user key from JWT or fallback to IP
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userKey = (authentication != null && authentication.isAuthenticated() && 
                         !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() // JWT username or userId
                : request.getRemoteAddr(); // Fallback to IP for unauthenticated users

        // Select appropriate bucket based on endpoint
        Bucket bucket = selectBucket(requestURI, userKey, authentication);

        // Try to consume a token from the bucket
        if (bucket.tryConsume(1)) {
            // Add rate limit headers for better UX
            long availableTokens = bucket.getAvailableTokens();
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));
            
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for user/IP: {} on endpoint: {}", userKey, requestURI);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Please try again later.\"}"
            );
        }
    }

    /**
     * Selects the appropriate bucket based on the request URI and user authentication.
     */
    private Bucket selectBucket(String requestURI, String userKey, Authentication authentication) {
        // Login endpoint - strictest limit (5 req/min)
        if (requestURI.contains("/api/users/login")) {
            logger.debug("Applying login rate limit for: {}", userKey);
            return rateLimiterService.resolveLoginBucket(userKey);
        }
        
        // Register endpoint - very strict limit (3 req/min)
        if (requestURI.contains("/api/users/register")) {
            logger.debug("Applying register rate limit for: {}", userKey);
            return rateLimiterService.resolveRegisterBucket(userKey);
        }
        
        // Payment endpoints - moderate limit (20 req/min)
        if (requestURI.startsWith("/api/payments")) {
            logger.debug("Applying payment rate limit for: {}", userKey);
            return rateLimiterService.resolvePaymentBucket(userKey);
        }

        // Product endpoints
        if (requestURI.startsWith("/api/products")) {
            // Cache refresh endpoint (very strict): POST /api/products/refresh-products-cache
            if (requestURI.startsWith("/api/products/refresh-products-cache")) {
                logger.debug("Applying product cache refresh rate limit for: {}", userKey);
                return rateLimiterService.resolveProductCacheBucket(userKey);
            }
            // Search endpoint: GET /api/products/search/{name}
            if (requestURI.startsWith("/api/products/search")) {
                logger.debug("Applying product search rate limit for: {}", userKey);
                return rateLimiterService.resolveProductSearchBucket(userKey);
            }
            // Filter endpoint: GET /api/products/filter
            if (requestURI.startsWith("/api/products/filter")) {
                logger.debug("Applying product filter rate limit for: {}", userKey);
                return rateLimiterService.resolveProductFilterBucket(userKey);
            }
            // General product browsing (list, by id, category)
            logger.debug("Applying product general rate limit for: {}", userKey);
            return rateLimiterService.resolveProductGeneralBucket(userKey);
        }
        
        // Admin endpoints - higher limit (200 req/min)
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            logger.debug("Applying admin rate limit for: {}", userKey);
            return rateLimiterService.resolveAdminBucket(userKey);
        }
        
        // Default limit for all other endpoints (100 req/min)
        logger.debug("Applying default rate limit for: {}", userKey);
        return rateLimiterService.resolveDefaultBucket(userKey);
    }
}
