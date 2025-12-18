package com.alura.foro.hub.api.security.exception;

public record FieldErrorItem(
        String field,
        String message
) {}
