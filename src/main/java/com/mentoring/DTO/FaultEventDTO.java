package com.mentoring.DTO;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Data
public class FaultEventDTO {
    private Long cctvId;
    private LocalDateTime timestamp;

    private Boolean icmpStatus;              
    private Double icmpAvgRttMs;            
    private Double icmpPacketLossPct;       
    private Boolean hlsStatus;

    private String eventCode;               
    private String severity;                 
    private String reason;                  
}
