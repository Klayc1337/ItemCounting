package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.OrderService;
import org.example.itemcounting.entity.ReplenishmentRequest;
import org.example.itemcounting.rest.dto.ReplenishmentRequestDTO;
import org.example.itemcounting.rest.dto.WaitOrderItemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/wait")
    public List<WaitOrderItemDTO> getWaitItems() {
        return orderService.getWaitItems();
    }

    @PostMapping("/replenishment")
    public ResponseEntity<ReplenishmentRequest> createRequest(@RequestBody ReplenishmentRequestDTO input) {
        return ResponseEntity.status(201).body(orderService.createReplenishmentRequest(input));
    }
}
