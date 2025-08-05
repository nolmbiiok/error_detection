package com.mentoring.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoring.entity.FaultEventEntity;
import com.mentoring.service.FaultEventService;
import com.mentoring.service.FaultRuleService;
import com.mentoring.service.RedisService;

import java.time.Duration;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class KafkaFaultConsumer {

    private final ObjectMapper objectMapper;
    private final FaultEventService faultEventService;
    private final RedisService redisService; // 🔸 Redis 추가

    public KafkaFaultConsumer(FaultEventService faultEventService, ObjectMapper objectMapper, RedisService redisService) {
        this.faultEventService = faultEventService;
        this.objectMapper = objectMapper;
        this.redisService = redisService;
    }

    @KafkaListener(topics = "health-events", groupId = "fault-detector-group")
    public void consume(FaultEventEntity event) {
        System.out.println("[Kafka] 변환된 객체: " + event);

        if ("HLS_TIMEOUT".equals(event.getFaultType())) {
            String key = "fault:hls_timeout:" + event.getCctvId();
            long count = redisService.increment(key, Duration.ofMinutes(10));
            event.setHlsTimeoutCount(count);

            if (count >= 3) {
                event.setSeverity(FaultEventEntity.Severity.HIGH);
                event.setReason("HLS_TIMEOUT 지속 장애 상태");
            }
        }
        faultEventService.checkAndProcess(event);
    }

}