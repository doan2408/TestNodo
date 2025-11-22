package com.example.coursemanagement.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSearchRequest {
    private String keyword; // Tìm theo name, code, description
    private String status; // "1" hoặc "0"

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "DESC";
}
