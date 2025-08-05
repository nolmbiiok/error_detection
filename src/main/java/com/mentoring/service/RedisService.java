package com.mentoring.service;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String INCR_WITH_TTL_LUA =
            "local current = redis.call('INCR', KEYS[1]); " +
            "if current == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]); end; " +
            "return current;";

    public long incrementWindow(String key, Duration window) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(INCR_WITH_TTL_LUA, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(window.toMillis())
        );
        return result == null ? 0L : result;
    }

    public void reset(String key) {
        redisTemplate.delete(key);
    }

    public long getCount(String key) {
        String v = redisTemplate.opsForValue().get(key);
        return (v == null) ? 0L : Long.parseLong(v);
    }
}
