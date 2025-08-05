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

    private final FaultEventService faultEventService;

    public KafkaFaultConsumer(FaultEventService faultEventService) {
        this.faultEventService = faultEventService;
    }

    @KafkaListener(topics = "health-events", groupId = "fault-detector-group")
    public void consume(FaultEventEntity event) {
        System.out.println("[Kafka] 변환된 객체: " + event);

        faultEventService.checkAndProcess(event);
    }

}