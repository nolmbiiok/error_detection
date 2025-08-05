package com.mentoring.kafka;

import com.mentoring.DTO.FaultAlertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaFaultProducer {

	private final KafkaTemplate<String, FaultAlertDTO> kafkaTemplate;

    public void sendAlert(FaultAlertDTO alertDto) {
        try {
            kafkaTemplate.send("alert-events", alertDto);
            log.info("[Kafka] Alert 전송 완료: {}", alertDto);
        } catch (Exception e) {
            log.error("[Kafka] Alert 전송 실패", e);
        }
    }
}
