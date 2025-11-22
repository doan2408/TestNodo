package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.request.StudentRequest;
import com.example.coursemanagement.dto.request.StudentSearchRequest;
import com.example.coursemanagement.dto.response.StudentResponse;
import com.example.coursemanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/selection")
    public ResponseEntity<List<StudentResponse>> getStudents() {
        return ResponseEntity.ok(studentService.getSelectStudents());
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        StudentSearchRequest request = StudentSearchRequest.builder()
                .keyword(keyword)
                .gender(gender)
                .status("1")
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        return ResponseEntity.ok(studentService.getAllStudents(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponse> createStudent(@Valid @ModelAttribute StudentRequest studentRequest) {
        return ResponseEntity.ok(studentService.createStudent(studentRequest));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable long id,
                                                         @Valid @ModelAttribute StudentRequest studentRequest
    ) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.softDeleteStudent(id));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStudents() {
        try {
            byte[] excelFile = studentService.exportAllStudentsToExcel();
            // Đặt tên file và header trả về
            String fileName = "students.xlsx";
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
