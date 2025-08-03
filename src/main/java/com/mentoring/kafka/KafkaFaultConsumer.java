package com.mentoring.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoring.entity.FaultEventEntity;
import com.mentoring.service.FaultRuleService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaFaultConsumer {

    private final FaultRuleService ruleService;
    private final ObjectMapper objectMapper;


    public KafkaFaultConsumer(FaultRuleService ruleService, ObjectMapper objectMapper) {
        this.ruleService = ruleService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "health-events", groupId = "fault-detector-group")
    public void consume(String message) {
        try {
            FaultEventEntity event = objectMapper.readValue(message, FaultEventEntity.class);
            System.out.println("[Kafka] 수신된 메시지: " + message);
            ruleService.checkRule(event);
        } catch (Exception e) {
            System.err.println("Kafka 메시지 파싱 실패: " + e.getMessage());
        }
    }

}
