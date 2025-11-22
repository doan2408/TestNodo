package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.request.CourseRequest;
import com.example.coursemanagement.dto.request.CourseSearchRequest;
import com.example.coursemanagement.dto.response.CourseResponse;
import com.example.coursemanagement.dto.response.MediaResponse;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.Image;
import com.example.coursemanagement.enums.FileType;
import com.example.coursemanagement.enums.ObjectType;
import com.example.coursemanagement.exception.DuplicateException;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.mapper.CourseMapper;
import com.example.coursemanagement.mapper.ImageMapper;
import com.example.coursemanagement.repository.CourseRepository;
import com.example.coursemanagement.repository.ImageRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final ImageRepository imageRepository;
    private final CourseMapper courseMapper;
    private final CloudinaryService cloudinaryService;
    private final ImageMapper imageMapper;
    private final MessageSource messageSource;

    public CourseService(CourseRepository courseRepository, ImageRepository imageRepository, CourseMapper courseMapper, CloudinaryService cloudinaryService, ImageMapper imageMapper, @Qualifier("messageSource") MessageSource messageSource) {
        this.courseRepository = courseRepository;
        this.imageRepository = imageRepository;
        this.courseMapper = courseMapper;
        this.cloudinaryService = cloudinaryService;
        this.imageMapper = imageMapper;
        this.messageSource = messageSource;
    }

    public Page<CourseResponse> getAllCourses(CourseSearchRequest request) {
        Sort sort = Sort.by(
                request.getSortDirection().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );

        String keyword = request.getKeyword() != null ? escapeLike(request.getKeyword().trim()) : "";

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Course> coursePage = courseRepository.searchCourses(keyword, "1", pageable);
        List<Course> courses = new ArrayList<>(coursePage.getContent());

        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .toList();

        List<Image> images = imageRepository.findAllActiveByObjectAndType(
                ObjectType.course,
                courseIds,
                FileType.thumbnail,
                "1"
        );

        // map 1 course - image list (thumbnail)
        Map<Long, List<Image>> thumbnailMap = images.stream().collect(Collectors.groupingBy(Image::getObjectId, Collectors.toList()));

        List<CourseResponse> responses = courseMapper.toResponseList(courses);
        responses.forEach(
                c -> {
                    List<Image> thumbnail = thumbnailMap.getOrDefault(c.getId(), List.of());
                    if (!thumbnail.isEmpty()) {
                        List<MediaResponse> mediaResponses = imageMapper.toResponseList(thumbnail);
                        c.setThumbnail(mediaResponses);
                    } else {
                        c.setThumbnail(null);
                    }
                }
        );
        return new PageImpl<>(responses, pageable, coursePage.getTotalElements());
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        List<String> errors = new ArrayList<>();
        // Validate code unique
        if (courseRepository.findByCodeAndActive(request.getCode(), "1").isPresent()) {
            errors.add("course.code.duplicate");
        }
        if (!errors.isEmpty()) {
            throw new DuplicateException(errors);
        }

        Course course = courseMapper.toEntity(request);

        Course savedCourse = courseRepository.save(course);

        // Upload thumbnail if exists
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            List<Image> thumbnailImages = uploadThumbnail(savedCourse.getId(), request.getThumbnail());
            savedCourse.getImages().addAll(thumbnailImages);
            CourseResponse response = courseMapper.toResponse(savedCourse);
            List<MediaResponse> mediaResponses = imageMapper.toResponseList(thumbnailImages);
            response.setThumbnail(mediaResponses);
            return response;
        }
        return courseMapper.toResponse(savedCourse);
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findActiveById(id, "1").orElseThrow(
                () -> new NotFoundException("course.not.found")
        );
        CourseResponse response = courseMapper.toResponse(course);
        List<Image> thumbnail = imageRepository.findActiveByObjectAndType(ObjectType.course, course.getId(), FileType.thumbnail, "1");
        List<MediaResponse> mediaResponses = imageMapper.toResponseList(thumbnail);
        response.setThumbnail(mediaResponses);
        return response;
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course courseExist = courseRepository.findActiveById(id, "1").orElseThrow(
                () -> new NotFoundException("course.not.found")
        );
        List<String> errors = new ArrayList<>();
        if (!Objects.equals(request.getCode(), courseExist.getCode())) {
            if (courseRepository.findByCodeAndActive(request.getCode(), "1").isPresent()) {
                errors.add("course.code.duplicate");
            }
            courseExist.setCode(request.getCode());
        }
        if (!errors.isEmpty()) {
            throw new DuplicateException(errors);
        }

        // request -> entity
        courseMapper.updateEntity(request, courseExist);

        // soft delete
        if (request.getDeleteThumbnailIds() != null && !request.getDeleteThumbnailIds().isEmpty()) {
            List<Image> imamgesToDelete = imageRepository.findAllByIdInAndStatus(request.getDeleteThumbnailIds(), "1");
            imamgesToDelete.forEach(img -> img.setStatus("0"));
            courseExist.getImages().addAll(imamgesToDelete);
        }

        // if there is a new images uploaded
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            List<Image> newThumbnail = uploadThumbnail(courseExist.getId(), request.getThumbnail());
            courseExist.getImages().addAll(newThumbnail);
        }

        Course updatedCourse = courseRepository.save(courseExist);
        CourseResponse response = courseMapper.toResponse(updatedCourse);
        List<MediaResponse> mediaResponses = imageMapper.toResponseList(
                imageRepository.findActiveByObjectAndType(ObjectType.course, updatedCourse.getId(), FileType.thumbnail, "1")
        );
        response.setThumbnail(mediaResponses);
        return response;
    }

    @Transactional
    public String softDeleteCourse(Long id) {
        Course course = courseRepository.findActiveById(id, "1").orElseThrow(
                () -> new NotFoundException("course.not.found")
        );
        course.setStatus("0");
        course.setUpdatedAt(new Date(System.currentTimeMillis()));
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("course.deleted", null, locale);
    }

    @Transactional
    public byte[] exportAllCoursesToExcel() throws IOException {
        List<Course> courses = courseRepository.findAllByStatus("1");

        // Tạo workbook Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Courses");

        // Tạo định dạng cho các cột ngày tháng
        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Code", "Description", "Status", "Created at", "Updated at"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // fill data to row
        int rowNum = 1;
        for (Course course : courses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(course.getId());
            row.createCell(1).setCellValue(course.getName());
            row.createCell(2).setCellValue(course.getCode());
            row.createCell(3).setCellValue(course.getDescription());
            row.createCell(4).setCellValue(course.getStatus());
            // Áp dụng định dạng ngày giờ cho các cột "Created at" và "Updated at"
            Cell createdAtCell = row.createCell(5);
            createdAtCell.setCellValue(course.getCreatedAt());
            createdAtCell.setCellStyle(dateCellStyle);  // Áp dụng định dạng

            Cell updatedAtCell = row.createCell(6);
            updatedAtCell.setCellValue(course.getUpdatedAt());
            updatedAtCell.setCellStyle(dateCellStyle);  // Áp dụng định dạng
        }

        // Tự động điều chỉnh độ rộng cột cho tất cả cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Đảm bảo cột chứa ngày giờ được đủ rộng nếu cần
        sheet.setColumnWidth(6, 20 * 256); // Cột "Created at"
        sheet.setColumnWidth(7, 20 * 256); // Cột "Updated at"
        // Tạo output stream để xuất file Excel
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);

        // Đóng workbook
        workbook.close();

        return byteArrayOutputStream.toByteArray(); // Trả về byte array của file Excel
    }


    private String escapeLike(String keyword) {
        if (keyword == null) return null;
        return keyword
                .replace("\\", "\\\\")   // escape backslash
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private List<Image> uploadThumbnail(Long courseId, List<MultipartFile> files) {
        // get max orderIndex
        Integer maxOrder = imageRepository.findMaxOrderIndex(
                ObjectType.course,
                courseId,
                FileType.thumbnail
        );
        System.out.println("maxOrder: " + maxOrder);
        List<Image> images = new ArrayList<>();
        int index = maxOrder;

        for (MultipartFile file : files) {
            Map<String, String> uploadResult = cloudinaryService.uploadFile(file);
            Image image = Image.builder()
                    .objectType(ObjectType.course)
                    .objectId(courseId)
                    .url(uploadResult.get("url"))
                    .publicId(uploadResult.get("public_id"))
                    .fileType(FileType.thumbnail)
                    .orderIndex(++index)
                    .status("1")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .build();
            images.add(image);
        }
        return images;
    }

    // use for select by card
    public List<CourseResponse> getSelectCourses() {
        return courseMapper.toResponseList(courseRepository.findAllByStatus("1"));
    }
}
