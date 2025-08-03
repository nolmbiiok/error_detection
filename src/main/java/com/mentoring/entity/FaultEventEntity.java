package com.mentoring.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "fault_logs")
@Data
public class FaultEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cctvId;

    private String faultType; // 예: "ICMP", "HLS"

    private LocalDateTime occurredAt;

    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String reason;

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
