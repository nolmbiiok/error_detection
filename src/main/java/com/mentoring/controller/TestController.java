package com.mentoring.controller;

import com.mentoring.service.FaultRuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final FaultRuleService ruleService;

    public TestController(FaultRuleService ruleService) {
        this.ruleService = ruleService;
    }


}
