package com.example.coursemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaResponse {
    private Long id;
    private String url;
    private String publicId;
    private String fileType;
    private Integer orderIndex;
    private String status;
    private Date createdAt;
}
