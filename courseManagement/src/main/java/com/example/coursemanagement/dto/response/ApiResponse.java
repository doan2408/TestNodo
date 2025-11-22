package com.example.coursemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private List<String> message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int status, List<String> message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data, List<String> messageKey) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(messageKey)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(List<String> messageKey) {
        return ApiResponse.<T>builder()
                .status(500)
                .message(messageKey)
                .data(null)
                .build();
    }
}
