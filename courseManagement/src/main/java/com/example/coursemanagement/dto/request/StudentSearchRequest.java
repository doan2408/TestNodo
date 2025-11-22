package com.example.coursemanagement.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSearchRequest {
    private String keyword; // Tìm theo name, email, phone
    private String status; // "1" hoặc "0"
    private String gender;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "DESC";
}
