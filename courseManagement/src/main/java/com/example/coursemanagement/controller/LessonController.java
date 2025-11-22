package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.request.LessonRequest;
import com.example.coursemanagement.dto.request.LessonSearchRequest;
import com.example.coursemanagement.dto.response.LessonResponse;
import com.example.coursemanagement.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lesson")
public class LessonController {
    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    // get lesson list by courseId
    @GetMapping("/{courseId}")
    public ResponseEntity<Page<LessonResponse>> getAllLessons(
            @PathVariable Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        LessonSearchRequest request = LessonSearchRequest.builder()
                .courseId(courseId)
                .keyword(keyword)
                .status("1")
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        return ResponseEntity.ok(lessonService.getLessonsByCourseId(request));
    }

    // detail lesson by lessonId
    @GetMapping("/detail/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonDetail(
            @PathVariable Long lessonId
    ) {
        return ResponseEntity.ok(lessonService.getLessonDetail(lessonId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonResponse> createLesson(@Valid @ModelAttribute LessonRequest request) {
        return ResponseEntity.ok(lessonService.createLesson(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable long id,
            @Valid @ModelAttribute LessonRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLesson(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.softDeleteLesson(id));
    }
}
