package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.OrderService;
import org.example.itemcounting.rest.dto.ReplenishmentStatusDTO;
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

    @GetMapping("/replenishment/{id}")
    public ReplenishmentStatusDTO status(@PathVariable Long id) {
        return orderService.getReplenishmentStatus(id);
    }

    @PostMapping("/replenishment")
    public ResponseEntity<ReplenishmentStatusDTO> createRequest(@RequestBody ReplenishmentRequestDTO input) {
        var request = orderService.createReplenishmentRequest(input);
        return ResponseEntity.status(201).body(new ReplenishmentStatusDTO(request.getId(), request.getStatus()));
    }
}
