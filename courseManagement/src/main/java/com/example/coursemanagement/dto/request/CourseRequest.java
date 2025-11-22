package com.example.coursemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequest {
    @NotBlank(message = "{course.name.required}")
    @Size(min = 3, max = 255, message = "{course.name.size}")
    private String name;

    @NotBlank(message = "{course.code.required}")
    @Pattern(regexp = "^[A-Za-z0-9]{3,50}$", message = "{course.code.invalid}")
    private String code;

    @Size(max = 500, message = "{course.description.size}")
    private String description;

    private List<MultipartFile> thumbnail;

    private List<Long> deleteThumbnailIds;
}
