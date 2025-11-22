package com.example.coursemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    private Long id;
    private Long courseId;
    private String title;
//    private Integer orderIndex;
    private String status;
    private List<MediaResponse> videos;
    private List<MediaResponse> thumbnails;
    private Date createdAt;
    private Date updatedAt;
}