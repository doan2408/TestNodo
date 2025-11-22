package com.example.coursemanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class NotFoundException extends RuntimeException {
    private List<String> errors;

    public NotFoundException(List<String> errors) {
        super("not found");
        this.errors = errors;
    }

    public NotFoundException(String messageKey) {
        super(messageKey);
        this.errors = List.of(messageKey);
    }

    public List<String> getErrors() {
        return errors;
    }

//    public NotFoundException(String message) {
//        super(message);
//    }
}
