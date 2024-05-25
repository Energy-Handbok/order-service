package com.khaphp.orderservice.exception;

public class ErrorFound extends RuntimeException{
    public ErrorFound(String message) {
        super(message);
    }
}
