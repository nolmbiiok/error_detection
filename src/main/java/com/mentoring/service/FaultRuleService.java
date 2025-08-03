package com.mentoring.service;

import com.mentoring.entity.FaultEventEntity;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaultRuleService {

    private final KieContainer kieContainer;
    private final FaultMonitorService faultMonitorService;  // Redis 연속 장애 감지 서비스

    public void checkRule(FaultEventEntity event) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(event); 
        kieSession.fireAllRules();
        kieSession.dispose();

        // 룰 실행 후 연속 장애 감지 수행
        faultMonitorService.trackFaultEvent(event);
    }

    public void evaluate(FaultEventEntity event) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(event);
        kieSession.fireAllRules();
        kieSession.dispose();

        // 선택적으로 사용 가능
        faultMonitorService.trackFaultEvent(event);
    }
}
