package com.example.coursemanagement.dto.response;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEnrollmentResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String avatarUrl;
    private Date enrolledAt;
}
