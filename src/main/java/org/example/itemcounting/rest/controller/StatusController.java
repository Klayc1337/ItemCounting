package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.BaseResponse;
import org.example.itemcounting.business.service.HealthCheckService;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StatusController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/status")
    public Mono<BaseResponse<InvoiceDTO>> getStatus(@RequestBody InvoiceDTO invoice) {
        return healthCheckService.validate(invoice);
    }
}
