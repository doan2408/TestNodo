package com.example.coursemanagement.mapper;


import com.example.coursemanagement.dto.request.StudentRequest;
import com.example.coursemanagement.dto.response.StudentResponse;
import com.example.coursemanagement.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "updatedAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "gender", source = "gender")
    Student toEntity(StudentRequest request);

    @Mapping(target = "avatar", ignore = true)
    StudentResponse toResponse(Student student);

    List<StudentResponse> toResponseList(List<Student> students);

    List<Student> toEntityList(List<StudentRequest> studentRequests);

    void updateEntity(StudentRequest request, @MappingTarget Student student);

    default java.sql.Date toSqlDate(long timeInMillis) {
        return new java.sql.Date(timeInMillis);
    }

}
