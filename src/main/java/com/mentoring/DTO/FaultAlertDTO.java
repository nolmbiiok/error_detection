package com.mentoring.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.mentoring.entity.FaultEventEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaultAlertDTO {
    private String groupId;             
    private Long cctvId;
    private List<String> users;         
    private String eventCode;           
    private FaultEventEntity.Severity severity;  
    private String reason;              
}
