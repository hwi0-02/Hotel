package com.example.backend.common.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 간단한 IP 기반 요청 레이트 리미터.
 * 새로고침/짧은 시간 내 반복 호출이 일정 횟수를 초과하면 429를 반환한다.
 * 1분에 10개 요청 제한
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final Logger suspiciousLog = LoggerFactory.getLogger("SUSPICIOUS");

    private static final Bandwidth LIMIT = Bandwidth.classic(
        10,
        Refill.greedy(10, Duration.ofMinutes(1))
    );

    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(Duration.ofMinutes(10))
        .build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // actuator, static 리소스는 제한하지 않음
        if (uri.startsWith("/actuator") || 
            uri.startsWith("/uploads") ||
            uri.startsWith("/favicon.ico")) {
            return true;
        }
        // 사전 요청(OPTIONS)은 제한하지 않는다.
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String uri = request.getRequestURI();
        
        Bucket bucket = bucketCache.get(clientIp, key -> {
            log.info("Creating new bucket for IP: {}", key);
            return Bucket.builder().addLimit(LIMIT).build();
        });

        long availableTokens = bucket.getAvailableTokens();
        
        if (bucket.tryConsume(1)) {
            log.debug("Request allowed - IP: {}, URI: {}, Remaining: {}", clientIp, uri, availableTokens - 1);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens - 1));
            response.setHeader("X-RateLimit-Limit", "10");
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rate limit exceeded - IP: {}, URI: {}", clientIp, uri);
        suspiciousLog.warn("Rate limit exceeded ip={} uri={}", clientIp, uri);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, "60");
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Limit", "10");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"요청이 너무 많습니다. 1분 후 다시 시도해주세요.\"}");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}
