package com.alura.foro.hub.api.security;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
