package com.example.coursemanagement.exception;

import java.util.List;

public class DuplicateException extends RuntimeException {
    private List<String> errors;
    public DuplicateException(List<String> errors) {
        super("Duplicate errors occurred");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}