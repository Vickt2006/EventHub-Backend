package com.eventhub.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {

        Map<String, String> m = new LinkedHashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(x -> m.put(
                        x.getField(),
                        x.getDefaultMessage()
                ));

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "success", false,
                        "errors", m
                ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception e) {

        // Actual error Eclipse Console me show hoga
        e.printStackTrace();

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "success", false,
                        "message", e.getMessage() != null
                                ? e.getMessage()
                                : "Internal server error"
                ));
    }
}