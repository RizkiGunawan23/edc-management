package com.rizki.edcmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.rizki.edcmanagement.dto.common.ErrorResponse;
import com.rizki.edcmanagement.model.enums.TerminalStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
                        WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message("Endpoint " + request.getDescription(false).replace("uri=", "") + " not found")
                                .build();
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                        WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex, WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message("Method " + ex.getMethod() + " not allowed for "
                                                + request.getDescription(false).replace("uri=", ""))
                                .build();
                return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
        }

        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex,
                        WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                        WebRequest request) {
                Map<String, List<String>> fieldErrors = new HashMap<>();

                // Group errors by field name
                ex.getBindingResult().getFieldErrors().forEach(error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
                });

                // Handle global errors (if any)
                ex.getBindingResult().getGlobalErrors().forEach(error -> {
                        String errorMessage = error.getDefaultMessage();
                        fieldErrors.computeIfAbsent("global", k -> new ArrayList<>()).add(errorMessage);
                });

                // Sort error messages for each field to ensure consistent order
                fieldErrors.forEach((fieldName, errorList) -> Collections.sort(errorList));

                ErrorResponse error = ErrorResponse.builder()
                                .errors(new HashMap<>(fieldErrors))
                                .build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                        WebRequest request) {
                Map<String, List<String>> fieldErrors = new HashMap<>();

                // Check if it's an InvalidFormatException (enum parsing error)
                Throwable cause = ex.getCause();
                if (cause instanceof InvalidFormatException) {
                        InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
                        Object value = invalidFormatException.getValue();
                        Class<?> targetType = invalidFormatException.getTargetType();

                        // Check if it's a TerminalStatus enum error
                        if (targetType != null && targetType.equals(TerminalStatus.class)) {
                                String fieldName = "status";
                                String errorMessage = String.format(
                                                "Invalid status value: '%s'. Valid values are: ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE",
                                                value);
                                fieldErrors.put(fieldName, Collections.singletonList(errorMessage));
                        } else {
                                // Generic enum error
                                String fieldName = invalidFormatException.getPath().isEmpty() ? "unknown"
                                                : invalidFormatException.getPath()
                                                                .get(invalidFormatException.getPath().size() - 1)
                                                                .getFieldName();
                                String errorMessage = String.format("Invalid value: '%s' for field '%s'", value,
                                                fieldName);
                                fieldErrors.put(fieldName, Collections.singletonList(errorMessage));
                        }
                } else {
                        // Generic JSON parsing error
                        fieldErrors.put("request", Collections.singletonList("Invalid JSON format or data type"));
                }

                ErrorResponse error = ErrorResponse.builder()
                                .errors(fieldErrors)
                                .build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .message("An unexpected error occurred")
                                .build();
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}