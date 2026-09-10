package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

//@RestController
//@RequiredArgsConstructor
//public class StatusController {
//
//    private final HealthCheckService healthCheckService;
//
//    @GetMapping("/status")
//    public Mono<String> getStatus() {
//        return healthCheckService.validate()
//                .map(result -> {
//                    if ("success".equals(result)) {
//                        return result;
//                    } else {
//                        return "Лежит: " + result;
//                    }
//                });
//    }
//}
