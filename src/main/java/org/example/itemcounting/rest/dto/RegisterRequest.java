package org.example.itemcounting.rest.dto;

public record RegisterRequest(String username, String password, String fullName, Long warehouseId) {}
