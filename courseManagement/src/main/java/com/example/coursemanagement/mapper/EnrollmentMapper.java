package com.example.coursemanagement.mapper;

import com.example.coursemanagement.dto.request.EnrollmentRequest;
import com.example.coursemanagement.dto.response.EnrollmentResponse;
import com.example.coursemanagement.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrollmentMapper {
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "updatedAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    Enrollment toEntity(EnrollmentRequest request);

    @Mapping(target = "studentId", source = "id.studentId")
    @Mapping(target = "studentName", source = "student.name")
//    @Mapping(target = "courseId", source = "id.courseId")
//    @Mapping(target = "courseName", source = "course.name")
    EnrollmentResponse toResponse(Enrollment enrollment);

    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);

    default java.sql.Date toSqlDate(long timeInMillis) {
        return new java.sql.Date(timeInMillis);
    }
}
