package com.mentoring.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieBuilder;
import org.kie.api.runtime.KieContainer;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

	@Bean
	public KieContainer kieContainer() {
	    KieServices ks = KieServices.Factory.get();
	    KieFileSystem kfs = ks.newKieFileSystem();

	    Resource drlFile = ResourceFactory.newClassPathResource("rules/fault-rules.drl", getClass());
	    kfs.write(ResourceFactory.newClassPathResource("rules/fault-rules.drl"));

	    ks.newKieBuilder(kfs).buildAll();
	    return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
	}

    @Bean
    public KieBase kieBase(KieContainer kieContainer) {
        return kieContainer.getKieBase();
    }

    @Bean
    public KieSession kieSession(KieContainer kieContainer) {
        return kieContainer.newKieSession(); 
    }
}
