package com.mentoring.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mentoring.entity.FaultEventEntity;

import java.time.Duration;

@Service
public class FaultMonitorService {

    private final RedisTemplate<String, Integer> redisTemplate;

    public FaultMonitorService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void trackFaultEvent(FaultEventEntity event) {
        String key = "fault:count:" + event.getCctvId();
        String alertKey = "fault:alerted:" + event.getCctvId();  // 중복 알림 방지용
       
        boolean isFault = !"NORMAL".equalsIgnoreCase(event.getSeverity().name());

        if (isFault) {
            Long count = redisTemplate.opsForValue().increment(key);

            // TTL이 설정되어 있지 않으면 30분 TTL 적용
            if (redisTemplate.getExpire(key) == -1) {
                redisTemplate.expire(key, Duration.ofMinutes(30));
            }

            // 연속 장애 3회 이상이면서 아직 알림이 전송되지 않았다면
            if (count != null && count >= 3 && !Boolean.TRUE.equals(redisTemplate.hasKey(alertKey))) {
                System.out.println("[ALERT] 연속 장애 감지: CCTV " + event.getCctvId());

                // 알림 발송 기록: TTL은 알림 재전송 가능 시점 고려 (예: 1시간)
                redisTemplate.opsForValue().set(alertKey, 1, Duration.ofHours(1));
            }

        } else {
            // 정상 응답 시 리셋
            redisTemplate.delete(key);
            redisTemplate.delete(alertKey);  // 알림 상태도 초기화
        }
    }
}
