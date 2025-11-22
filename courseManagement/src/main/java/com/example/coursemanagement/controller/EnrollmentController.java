package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.request.EnrollmentRequest;
import com.example.coursemanagement.dto.response.EnrollmentBatchResponse;
import com.example.coursemanagement.dto.response.EnrollmentResponse;
import com.example.coursemanagement.dto.response.StudentResponse;
import com.example.coursemanagement.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // get student list by courseId
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudentResponse>> getEnrollments(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(enrollmentService.getStudentsByCourse(courseId));
    }

    // get course list by 1 studentId
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getCourseList(@PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentBatchResponse> enrollStudent(
            @RequestBody @Validated EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.enrollStudentToCourses(request));
    }

    @PutMapping
    public ResponseEntity<EnrollmentBatchResponse> updateEnrollment(
            @RequestBody @Validated EnrollmentRequest request
    ) {
        return ResponseEntity.ok(enrollmentService.updateEnrollments(request));
    }

    @DeleteMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<String> deleteEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(enrollmentService.deleteEnrollment(studentId, courseId));
    }
}
