package com.example.coursemanagement.mapper;

import com.example.coursemanagement.dto.request.LessonRequest;
import com.example.coursemanagement.dto.response.LessonResponse;
import com.example.coursemanagement.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    @Mapping(target = "updatedAt", expression = "java(toSqlDate(System.currentTimeMillis()))")
    Lesson toEntity(LessonRequest request);

    @Mapping(target = "videos", ignore = true)
    @Mapping(target = "thumbnails", ignore = true)
    @Mapping(target = "courseId", source = "course.id")
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toResponses(List<Lesson> lessons);

    List<Lesson> toLessons(List<LessonRequest> lessonRequests);

    @Mapping(target = "course", ignore = true)
    void updateEntity(LessonRequest request, @MappingTarget Lesson lesson);

    default java.sql.Date toSqlDate(long timeInMillis) {
        return new java.sql.Date(timeInMillis);
    }
}
