package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.request.LessonRequest;
import com.example.coursemanagement.dto.request.LessonSearchRequest;
import com.example.coursemanagement.dto.response.LessonResponse;
import com.example.coursemanagement.dto.response.MediaResponse;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.Image;
import com.example.coursemanagement.entity.Lesson;
import com.example.coursemanagement.enums.FileType;
import com.example.coursemanagement.enums.ObjectType;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.mapper.ImageMapper;
import com.example.coursemanagement.mapper.LessonMapper;
import com.example.coursemanagement.repository.CourseRepository;
import com.example.coursemanagement.repository.ImageRepository;
import com.example.coursemanagement.repository.LessonRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final CloudinaryService cloudinaryService;
    private final MessageSource messageSource;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository, LessonMapper lessonMapper, CloudinaryService cloudinaryService, MessageSource messageSource, ImageRepository imageRepository, ImageMapper imageMapper, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonMapper = lessonMapper;
        this.cloudinaryService = cloudinaryService;
        this.messageSource = messageSource;
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        Lesson lesson = lessonMapper.toEntity(request);
//        Integer indexOrder = lessonRepository.findMaxOrderIndex(request.getCourseId(), "1");

        Course course = courseRepository.findActiveById(request.getCourseId(), "1")
                .orElseThrow(() -> new NotFoundException("course.not.found"));
        lesson.setCourse(course);
//        lesson.setOrderIndex(++indexOrder);

        Lesson savedLesson = lessonRepository.save(lesson);

        LessonResponse response = lessonMapper.toResponse(savedLesson);

        Integer maxOrderThumbnail = getMaxOrder(
                savedLesson.getId(),
                FileType.video
        );
        Integer indexThumbnail = maxOrderThumbnail != null ? maxOrderThumbnail : 0;
        if (request.getThumbnails() != null && !request.getThumbnails().isEmpty()) {
            List<Image> thumbnail = uploadMedia(FileType.thumbnail,
                    savedLesson.getId(),
                    indexThumbnail,
                    request.getThumbnails()
            );
            savedLesson.getImages().addAll(thumbnail);
            List<MediaResponse> mediaResponses = imageMapper.toResponseList(thumbnail);
            response.setThumbnails(mediaResponses);
        }

        Integer maxOrderVideo = getMaxOrder(
                savedLesson.getId(),
                FileType.video
        );
        Integer indexVideo = maxOrderVideo != null ? maxOrderVideo : 0;
        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
            List<Image> video = uploadMedia(FileType.video,
                    savedLesson.getId(),
                    indexVideo,
                    request.getVideos()
            );
            savedLesson.getImages().addAll(video);
            List<MediaResponse> mediaResponses = imageMapper.toResponseList(video);
            response.setVideos(mediaResponses);
        }
        return response;
    }

    public Page<LessonResponse> getLessonsByCourseId(LessonSearchRequest request) {
        courseRepository.findActiveById(request.getCourseId(), "1")
                .orElseThrow(() -> new NotFoundException("course.not.found"));

        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );
        String keyword = request.getKeyword() != null ? escapeLike(request.getKeyword().trim()) : "";

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Lesson> lessonsPage = lessonRepository.searchLesson(
                request.getCourseId(),
                "1",
                keyword,
                pageable
        );

        List<Lesson> lessons = new ArrayList<>(lessonsPage.getContent());
        if (lessons.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, lessonsPage.getTotalElements());
        }

        // get list of lesson id to query thumbnail + video
        List<Long> lessonIds = lessons.stream()
                .map(Lesson::getId)
                .toList();

        List<Image> thumbnailList = imageRepository.findAllActiveByObjectAndType(
                ObjectType.lesson,
                lessonIds,
                FileType.thumbnail,
                "1"
        );

        List<Image> videoList = imageRepository.findAllActiveByObjectAndType(
                ObjectType.lesson,
                lessonIds,
                FileType.video,
                "1"
        );

        Map<Long, List<Image>> thumbnailMap = thumbnailList.stream()
                .collect(Collectors.groupingBy(Image::getObjectId));

        Map<Long, List<Image>> videoMap = videoList.stream()
                .collect(Collectors.groupingBy(Image::getObjectId));

        List<LessonResponse> responses = lessonMapper.toResponses(lessons);
        responses.forEach(response -> {
            List<Image> thumbnail = thumbnailMap.getOrDefault(response.getId(), List.of());
            response.setThumbnails(thumbnail.isEmpty() ? null : imageMapper.toResponseList(thumbnail));

            List<Image> video = videoMap.getOrDefault(response.getId(), List.of());
            response.setVideos(video.isEmpty() ? null : imageMapper.toResponseList(video));
        });
        return new PageImpl<>(responses, pageable, lessonsPage.getTotalElements());
    }

    public LessonResponse getLessonDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findActiveById(lessonId, "1")
                .orElseThrow(() -> new NotFoundException("lesson.not.found"));

        List<Image> thumbnails = imageRepository.findAllActiveByObjectAndType(
                ObjectType.lesson,
                List.of(lessonId),
                FileType.thumbnail,
                "1"
        );

        List<Image> videos = imageRepository.findAllActiveByObjectAndType(
                ObjectType.lesson,
                List.of(lessonId),
                FileType.video,
                "1"
        );

        LessonResponse response = lessonMapper.toResponse(lesson);
        response.setThumbnails(thumbnails.isEmpty() ? null : imageMapper.toResponseList(thumbnails));
        response.setVideos(videos.isEmpty() ? null : imageMapper.toResponseList(videos));
        return response;
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest request) {
        Lesson lessonExist = lessonRepository.findActiveById(lessonId, "1")
                .orElseThrow(() -> new NotFoundException("lesson.not.found"));

        lessonMapper.updateEntity(request, lessonExist);
        // soft delete
        if (request.getDeleteThumbnailIds() != null && !request.getDeleteThumbnailIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllByIdInAndStatus(request.getDeleteThumbnailIds(), "1");
            imagesToDelete.forEach(img -> img.setStatus("0"));
            lessonExist.getImages().addAll(imagesToDelete);
        }

        if (request.getDeleteVideoIds() != null && !request.getDeleteVideoIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllByIdInAndStatus(request.getDeleteVideoIds(), "1");
            imagesToDelete.forEach(img -> img.setStatus("0"));
            lessonExist.getImages().addAll(imagesToDelete);
        }

        if(request.getCourseId() !=null) {
            Course course = courseRepository.findActiveById(request.getCourseId(), "1").orElseThrow(
                    () -> new NotFoundException("course.not.found")
            );
            lessonExist.setCourse(course);
        }

        Lesson updatedLesson = lessonRepository.save(lessonExist);

        LessonResponse response = lessonMapper.toResponse(updatedLesson);


        List<MultipartFile> newThumbnail = filterValidFiles(request.getThumbnails());
        if (!newThumbnail.isEmpty()) {
            Integer maxOrderThumbnail = getMaxOrder(lessonExist.getId(), FileType.thumbnail);
            Integer indexThumbnail = maxOrderThumbnail != null ? maxOrderThumbnail : 0;

            // Upload các ảnh thumbnail mới
            List<Image> uploadThumbnail = uploadMedia(FileType.thumbnail, lessonExist.getId(), indexThumbnail, newThumbnail);

            // Thêm các ảnh mới vào danh sách ảnh của lesson
            lessonExist.getImages().addAll(uploadThumbnail);

            // Đưa vào response
            response.setThumbnails(imageMapper.toResponseList(uploadThumbnail)); // Set lại response
        }
        else {
            // Nếu không có thumbnail mới, lấy thumbnail cũ từ database và đưa vào response
            List<Image> existingThumbnails = imageRepository.findAllActiveByObjectAndType(ObjectType.lesson, List.of(lessonExist.getId()), FileType.thumbnail, "1");
            response.setThumbnails(imageMapper.toResponseList(existingThumbnails));
        }


        // Xử lý upload video mới (nếu có)
        List<MultipartFile> newVideo = filterValidFiles(request.getVideos());
        if (!newVideo.isEmpty()) {
            Integer maxOrderVideo = getMaxOrder(lessonExist.getId(), FileType.video);
            Integer indexVideo = maxOrderVideo != null ? maxOrderVideo : 0;

            // Upload các video mới
            List<Image> uploadVideo = uploadMedia(FileType.video, lessonExist.getId(), indexVideo, newVideo);

            // Thêm các video mới vào danh sách ảnh của lesson
            lessonExist.getImages().addAll(uploadVideo);

            // Đưa vào response
            response.setVideos(imageMapper.toResponseList(uploadVideo)); // Set lại response
        }
        else {
            // Nếu không có video mới, lấy video cũ từ database và đưa vào response
            List<Image> existingVideos = imageRepository.findAllActiveByObjectAndType(ObjectType.lesson, List.of(lessonExist.getId()),FileType.video, "1");
            response.setVideos(imageMapper.toResponseList(existingVideos));
        }
        return response;
    }

    @Transactional
    public String softDeleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findActiveById(lessonId, "1").orElseThrow(
                () -> new NotFoundException("lesson.not.found")
        );
        lesson.setStatus("0");
        lesson.setUpdatedAt(new Date(System.currentTimeMillis()));
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("lesson.deleted", null, locale);
    }

    private String escapeLike(String keyword) {
        if (keyword == null) return null;
        return keyword
                .replace("\\", "\\\\")   // escape backslash
                .replace("%", "\\%")
                .replace("_", "\\_");
    }


    private Integer getMaxOrder(Long objectId,
                                FileType fileType) {
        return imageRepository.findMaxOrderIndex(
                ObjectType.lesson, objectId, fileType
        );
    }

    private List<Image> uploadMedia(FileType fileType,
                                    Long objectId, Integer orderIndex,
                                    List<MultipartFile> files
    ) {
        List<Image> images = new ArrayList<>();

        for (MultipartFile file : files) {
            Map<String, String> thumbnailResult = cloudinaryService.uploadFile(file);
            Image image = Image.builder()
                    .objectType(ObjectType.lesson)
                    .objectId(objectId)
                    .url(thumbnailResult.get("url"))
                    .publicId(thumbnailResult.get("public_id"))
                    .fileType(fileType)
                    .orderIndex(++orderIndex)
                    .status("1")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .build();
            images.add(image);
        }
        return images;
    }

    private List<MultipartFile> filterValidFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        return files.stream()
                .filter(file -> file != null
                                && file.getOriginalFilename() != null
                                && !file.getOriginalFilename().isEmpty()
                                && file.getSize() > 0) // check valid size of file
                .collect(Collectors.toList());
    }
}
