package com.example.coursemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private List<MediaResponse> thumbnail;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
