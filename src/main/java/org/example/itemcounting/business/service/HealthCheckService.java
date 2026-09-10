package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.BaseResponse;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.rest.dto.RequestValue;
import org.example.itemcounting.rest.dto.ResponseValue;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthCheckService {
    private final WebClient webClient;

    public Mono<BaseResponse<InvoiceDTO>> validate(InvoiceDTO invoice) {
        RequestValue requestBody = new RequestValue("", 0);

        return webClient.post()
                .uri("/api/validate")
                .header("RequestID", UUID.randomUUID().toString())
                .bodyValue(invoice)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<BaseResponse<InvoiceDTO>>() {})
                .flatMap(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        BaseResponse<InvoiceDTO> baseResponse = response.getBody();
                        if (baseResponse == null) {
                            return Mono.error(new RuntimeException("response body пустое"));
                        }

                        if ("success".equals(baseResponse.getStatus())) {
                            if (baseResponse.getData() != null) {
                                return Mono.just(baseResponse);
                            } else {
                                return Mono.error(new RuntimeException("статус Success но data пустое"));
                            }
                        } else {
                            List<String> errors = baseResponse.getErrors();
                            if(errors != null && !errors.isEmpty()) {
                                return Mono.just(baseResponse);
                            } else {
                                return Mono.error(new RuntimeException("errors пустое"));
                            }
                        }
                    } else {
                        return Mono.error(new RuntimeException("ошибка HTTP" + response.getStatusCode()));
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Исключения: " + e.getMessage(), e)));
    }
}