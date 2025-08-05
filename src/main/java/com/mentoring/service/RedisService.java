package com.mentoring.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public long increment(String key, Duration ttl) {
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, ttl);
        return count != null ? count : 0;
    }

    public void reset(String key) {
        redisTemplate.delete(key);
    }
}
