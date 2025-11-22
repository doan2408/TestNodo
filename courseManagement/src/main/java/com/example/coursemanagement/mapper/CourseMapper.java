package com.example.coursemanagement.mapper;

import com.example.coursemanagement.dto.request.CourseRequest;
import com.example.coursemanagement.dto.response.CourseResponse;
import com.example.coursemanagement.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "updatedAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "images", ignore = true)
    Course toEntity(CourseRequest request);

    @Mapping(target = "thumbnail", ignore = true)
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);

    List<Course> toEntityList(List<CourseRequest> courseRequests);

    void updateEntity(CourseRequest request, @MappingTarget Course course);

    default java.sql.Date toSqlDate(long timeInMillis) {
        return new java.sql.Date(timeInMillis);
    }
}
