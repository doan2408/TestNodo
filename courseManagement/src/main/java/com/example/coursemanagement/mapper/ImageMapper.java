package com.example.coursemanagement.mapper;

import com.example.coursemanagement.dto.response.MediaResponse;
import com.example.coursemanagement.entity.Image;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    MediaResponse toResponse(Image image);

    List<MediaResponse> toResponseList(List<Image> images);
}
