package org.example.itemcounting.rest.dto;
import org.example.itemcounting.enums.RequestStatus;
public record ReplenishmentStatusDTO(Long id, RequestStatus status) {}
