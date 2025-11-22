package com.example.coursemanagement.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentBatchResponse {
    private Long studentId;
    private String studentName;
    private List<CourseEnrollmentInfo> courses;
    private int successCount;
    private int failCount;
}

