package io.github.cctyl.controller;

import io.github.cctyl.aop.MethodCallCounterAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Profile("dev")
public class MethodCallCounterController {

    @Autowired
    private MethodCallCounterAspect methodCallCounterAspect;

    @GetMapping("/method-call-counts")
    public List<Map.Entry<String, Integer>> getMethodCallCounts() {
        return methodCallCounterAspect.getMethodCallCountMap().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
}
