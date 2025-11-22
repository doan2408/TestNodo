package com.example.coursemanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "{enrollment.studentId.required}")
    private Long studentId;

    @NotEmpty(message = "{enrollment.courseIds.required}")
    private List<Long> courseIds;


}
