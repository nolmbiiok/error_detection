package com.mentoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "fault_events", uniqueConstraints = {
	    @UniqueConstraint(columnNames = {"cctv_id", "fault_type"})
	})
public class FaultEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cctvId; 
    
    private String faultType;
    private LocalDateTime occurredAt;
    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String reason;
    private long hlsTimeoutCount; 
    
  
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
