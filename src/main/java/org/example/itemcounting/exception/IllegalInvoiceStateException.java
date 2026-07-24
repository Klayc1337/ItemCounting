package org.example.itemcounting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalInvoiceStateException extends RuntimeException {
    public IllegalInvoiceStateException(String message) {
        super(message);
    }
}
