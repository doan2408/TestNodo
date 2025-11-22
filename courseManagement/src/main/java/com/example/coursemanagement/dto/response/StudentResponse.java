package com.example.coursemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponse {
    private Long id;
    private String name;
    private String gender;
    private String email;
    private String phone;
    private List<MediaResponse> avatar;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
