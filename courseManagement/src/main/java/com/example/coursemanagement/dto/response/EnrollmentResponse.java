package com.example.coursemanagement.dto.response;

import lombok.*;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String gender;
    private List<CourseEnrollmentInfo> courses;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
