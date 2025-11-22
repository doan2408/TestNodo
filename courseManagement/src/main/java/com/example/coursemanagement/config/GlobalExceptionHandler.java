package com.example.coursemanagement.config;

import com.example.coursemanagement.dto.response.ApiResponse;
import com.example.coursemanagement.exception.DuplicateException;
import com.example.coursemanagement.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        List<String> errors = ex.getErrors();
        List<String> message = getMessage(errors);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleDuplicate(DuplicateException ex) {
        List<String> errors = ex.getErrors();
        List<String> localizedErrors = getMessage(errors);
        log.error("handleDuplicate: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(localizedErrors));
    }

    // catch validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = messageSource.getMessage(error, locale);
            errors.put(field, message);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    private List<String> getMessage(List<String> keys) {
        List<String> messages = new ArrayList<>();
        for (String key : keys) {
            try {
                String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
                messages.add(message);
            } catch (Exception e) {
                messages.add(key);
            }
        }
        return messages;
    }
}

