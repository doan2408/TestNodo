package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.request.CourseRequest;
import com.example.coursemanagement.dto.request.CourseSearchRequest;
import com.example.coursemanagement.dto.response.CourseResponse;
import com.example.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/course")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/selection")
    public ResponseEntity<?> getCourseSelection() {
        return ResponseEntity.ok(courseService.getSelectCourses());
    }

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        CourseSearchRequest request = CourseSearchRequest.builder()
                .keyword(keyword)
                .status("1")
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        return ResponseEntity.ok(courseService.getAllCourses(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponse> createCourse(@Valid @ModelAttribute CourseRequest courseRequest) {
        return ResponseEntity.ok(courseService.createCourse(courseRequest));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable long id,
            @Valid @ModelAttribute CourseRequest courseRequest) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable long id) {
        return ResponseEntity.ok(courseService.softDeleteCourse(id));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStudents() {
        try {
            byte[] excelFile = courseService.exportAllCoursesToExcel();
            // Đặt tên file và header trả về
            String fileName = "courses.xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);

            // Trả về file dưới dạng byte array
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



}
