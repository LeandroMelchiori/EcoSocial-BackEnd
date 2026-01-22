// infra/errors/GlobalExceptionHandler.java
package com.alura.foro.hub.api.security.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ 400 - Validaciones @Valid (body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldErrorItem> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        var body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validación fallida",
                req.getRequestURI(),
                fields
        );

        return ResponseEntity.badRequest().body(body);
    }

    // ✅ 404 - EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ✅ 409 - violación de constraints
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Conflicto de datos (constraint/relación).",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ✅ 403 - permisos
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tenés permisos para realizar esta acción.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            HttpServletRequest req
    ) {
        var body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Falta el part requerido: " + ex.getRequestPartName(),
                req.getRequestURI(),
                List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(com.fasterxml.jackson.core.JsonProcessingException.class)
    public ResponseEntity<ApiError> handleJsonBad(com.fasterxml.jackson.core.JsonProcessingException ex,
                                                  HttpServletRequest req) {

        var body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "El part 'data' debe ser un JSON válido.",
                req.getRequestURI(),
                List.of()
        );

        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    // ✅ 401 - auth (token inválido, no autenticado, etc.)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "No autenticado o credenciales inválidas.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "No encontrado: " + req.getRequestURI(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        var status = ex.getStatusCode();
        var body = ApiError.of(
                status.value(),
                status.toString(),
                ex.getReason() != null ? ex.getReason() : "Error",
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    // ✅ para ErrorResponseException
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponseException(ErrorResponseException ex, HttpServletRequest req) {
        var status = ex.getStatusCode();
        var body = ApiError.of(
                status.value(),
                status.toString(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    // ✅ 400 - IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ✅ 409 - error de negocio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest req) {
        var body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }


    // ✅ 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {

        log.error("🔥 500 en {} {} - {}", req.getMethod(), req.getRequestURI(), ex.toString(), ex);

        var body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error inesperado.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    private FieldErrorItem toFieldError(FieldError fe) {
        return new FieldErrorItem(fe.getField(), fe.getDefaultMessage());
    }
}
