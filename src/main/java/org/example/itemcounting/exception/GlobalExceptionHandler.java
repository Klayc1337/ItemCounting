package org.example.itemcounting.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @ExceptionHandler({DuplicateSkuException.class, InvalidQuantityException.class})
    public ErrorResponse handleBadRequest(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleNotFound(EntityNotFoundException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({IllegalInvoiceStateException.class, InsufficientStockException.class})
    public ErrorResponse handleConflict(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT);
    }

    private ErrorResponse buildResponse(RuntimeException ex, HttpStatus status) {
        log.error(ex.getMessage(), ex);
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
    }
}
