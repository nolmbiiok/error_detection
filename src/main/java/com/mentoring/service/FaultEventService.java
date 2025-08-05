package com.mentoring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mentoring.DTO.FaultAlertDTO;
import com.mentoring.entity.FaultEventEntity;
import com.mentoring.kafka.KafkaFaultProducer;
import com.mentoring.repository.FaultEventRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FaultEventService {

    private final FaultRuleService faultRuleService; 
    private final FaultEventRepository repository;
    private final KafkaFaultProducer kafkaFaultProducer; 

    public void checkAndProcess(FaultEventEntity incomingEvent) {
    	
    	applyBasicMapping(incomingEvent);
        faultRuleService.checkWithRules(incomingEvent);


        if ("ICMP_OK".equals(incomingEvent.getFaultType())) {
            repository.deleteByCctvIdAndFaultType(incomingEvent.getCctvId(), "ICMP_TIMEOUT");
            repository.deleteByCctvIdAndFaultType(incomingEvent.getCctvId(), "ICMP_LOSS");
        }
        if ("HLS_OK".equals(incomingEvent.getFaultType())) {
            repository.deleteByCctvIdAndFaultType(incomingEvent.getCctvId(), "HLS_TIMEOUT");
            repository.deleteByCctvIdAndFaultType(incomingEvent.getCctvId(), "HLS_ERROR");
        }
        
        FaultEventEntity saved = repository.findByCctvIdAndFaultType(
        	    incomingEvent.getCctvId(), incomingEvent.getFaultType()
        	).map(existing -> {
        	    existing.setOccurredAt(incomingEvent.getOccurredAt());
        	    existing.setResolvedAt(incomingEvent.getResolvedAt());
        	    existing.setSeverity(incomingEvent.getSeverity());
        	    existing.setReason(incomingEvent.getReason());
        	    return existing;
        	}).orElse(incomingEvent);

        	repository.save(saved);
        
        
        if (saved.getSeverity() == FaultEventEntity.Severity.MEDIUM ||
                saved.getSeverity() == FaultEventEntity.Severity.HIGH) {

        		FaultAlertDTO alertDto = FaultAlertDTO.builder()
        		    .groupId("mock-group-id")
        		    .cctvId(saved.getCctvId())
        		    .users(List.of("user1@example.com", "user2@example.com"))
        		    .eventCode(saved.getFaultType())
        		    .severity(saved.getSeverity())
        		    .reason(saved.getReason())
        		    .build();
                kafkaFaultProducer.sendAlert(alertDto);
            }
    }
    private void applyBasicMapping(FaultEventEntity event) {
        switch (event.getFaultType()) { 
        

            case "ICMP_OK", "HLS_OK" -> {
                event.setSeverity(FaultEventEntity.Severity.LOW); 
                event.setReason("정상 응답");
            }
            case "ICMP_LOSS" -> {
                event.setSeverity(FaultEventEntity.Severity.MEDIUM);
                event.setReason("일시적 장애");
            }
            case "HLS_TIMEOUT", "ICMP_TIMEOUT", "ICMP_FAILED", "HLS_NOT_FOUND", "HLS_ERROR", "RTSP_DOWN" -> {
                event.setSeverity(FaultEventEntity.Severity.HIGH);
                event.setReason("심각한 장애");
            }
            default -> {
                event.setSeverity(FaultEventEntity.Severity.MEDIUM);
                event.setReason("알 수 없는 이벤트");
            }
        }
    }
}

