package com.example.coursemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LessonRequest {
    @NotNull(message = "{lesson.courseId.required}")
    private Long courseId;

    @NotBlank(message = "{lesson.title.required}")
    @Size(min = 3, max = 255, message = "{lesson.title.size}")
    private String title;

    private List<MultipartFile> videos;

    private List<MultipartFile> thumbnails;

    private List<Long> deleteVideoIds;
    private List<Long> deleteThumbnailIds;
}
