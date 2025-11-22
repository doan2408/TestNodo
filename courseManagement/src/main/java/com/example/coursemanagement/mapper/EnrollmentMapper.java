package com.example.coursemanagement.mapper;

import com.example.coursemanagement.dto.response.EnrollmentResponse;
import com.example.coursemanagement.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {
    @Mapping(target = "studentId", source = "id.studentId")
    @Mapping(target = "studentName", source = "student.name")
    @Mapping(target = "courseId", source = "id.courseId")
    @Mapping(target = "courseName", source = "course.name")
    EnrollmentResponse toResponse(Enrollment enrollment);
}
