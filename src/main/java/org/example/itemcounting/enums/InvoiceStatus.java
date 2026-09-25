package org.example.itemcounting.enums;

public enum InvoiceStatus {
    DRAFT,        // создана, но еще не проведена
    CONFIRMED,    // подтверждена
    IN_PROGRESS,  // выполняется / товар в движении
    COMPLETED,    // полностью проведена
    CANCELLED,
    WAIT
}
