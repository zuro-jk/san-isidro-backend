package com.sanisidro.restaurante.core.exceptions;

import com.sanisidro.restaurante.core.aws.exception.AccessDeniedException;
import com.sanisidro.restaurante.core.aws.exception.FileNotFoundException;
import com.sanisidro.restaurante.core.aws.exception.FileUploadException;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<Object>> buildResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null));
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleOAuth2Exception(OAuth2AuthenticationException ex) {
        ex.printStackTrace(); // log completo en consola
        OAuth2Error error = ex.getError();
        String errorDescription = error.getDescription() != null ? error.getDescription() : error.getErrorCode();
        String message = "OAuth2 Authentication failed: " + errorDescription;
        return buildResponse(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileNotFound(FileNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUpload(FileUploadException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileAccessDenied(AccessDeniedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            ProductNotFoundException.class,
            CategoryNotFoundException.class,
            InventoryNotFoundException.class,
            EntityNotFoundException.class,
    })
    public ResponseEntity<ApiResponse<Object>> handleNotFound(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateProduct(DuplicateProductException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(ConflictException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            InvalidReservationException.class,
            BadRequestException.class,
            InvalidPointsOperationException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(InvalidRefreshTokenException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiResponse<Object>> handleTooManyAttempts(TooManyAttemptsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError fe) {
                fieldName = fe.getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = new ApiResponse<>(false, "Errores de validación", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message != null && message.contains("ReservationStatus")) {
            message = "Valor inválido para status. Valores permitidos: PENDING, CONFIRMED, CANCELLED";
        } else {
            message = "Request malformado";
        }
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // log completo
        return buildResponse("Ocurrió un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InventoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInventoryAlreadyExists(InventoryAlreadyExistsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
