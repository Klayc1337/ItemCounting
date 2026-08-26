package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.rest.dto.RequestValue;
import org.example.itemcounting.rest.dto.ResponseValue;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthCheckService {
    private final WebClient webClient;

    public Mono<String> validate() {
        RequestValue requestBody = new RequestValue("", 0);

        return webClient.post()
                .uri("/api/validate")
                .header("RequestID", UUID.randomUUID().toString())
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(ResponseValue.class)
                .flatMap(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        ResponseValue body = response.getBody();

                        if (body != null && (body.errors() == null || body.errors().isEmpty())) {
                            return Mono.just("success");
                        } else {
                            String error = body != null ? String.join(", ", body.errors()) : "неизвестные ошибки";
                            return Mono.just("Ошибки: " + error);
                        }
                    } else {
                        return Mono.just("http ошибка: " + response.getStatusCode());
                    }
                })
                .onErrorResume(e -> Mono.just("Исключение: " + e.getMessage()));
    }
}


//private final WebClient webClient;
//
//public boolean serviceAlive() {
//    try {
//        String requestId = UUID.randomUUID().toString();
//        ResponseEntity<String> response = webClient.post()
//                .uri("/actuator/health")
//                .header("HealthReq", requestId)
//                .
//                    .retrieve()
//                .toEntity(String.class)
//
//
//        return response != null && response.getStatusCode().is2xxSuccessful();
//    } catch (Exception e) {
//        return false;
//    }
//}
