package com.example.coursemanagement.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentInfo {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private boolean success;
    private String message;
}
