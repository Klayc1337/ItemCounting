package org.example.itemcounting.exception;

public class IllegalInvoiceStateException extends RuntimeException {
    public IllegalInvoiceStateException(String message) {
        super(message);
    }
}
