package com.mentoring.service;

import com.mentoring.entity.FaultEventEntity;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class FaultRuleService {

    private final KieContainer kieContainer;

    public void checkWithRules(FaultEventEntity event) {
    	
        KieSession kieSession = kieContainer.newKieSession();
        Collection<KiePackage> kiePackages = kieSession.getKieBase().getKiePackages();
        int totalRuleCount = 0;



		System.out.println("=== Loaded Drools Rules ===");
		for (KiePackage pkg : kiePackages) {
		    for (org.kie.api.definition.rule.Rule rule : pkg.getRules()) {
		        System.out.println("📦 룰 로드됨: " + rule.getName());
		        totalRuleCount++;
		    }
		}
		System.out.println("총 로드된 룰 개수: " + totalRuleCount);
		System.out.println("===========================");
        
        
        
        kieSession.insert(event);
        int count = kieSession.fireAllRules(); 
        System.out.println("Fired rules count: " + count);
        
        kieSession.dispose();
    }
}

