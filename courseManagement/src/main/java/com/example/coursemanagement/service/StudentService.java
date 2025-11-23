package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.request.StudentRequest;
import com.example.coursemanagement.dto.request.StudentSearchRequest;
import com.example.coursemanagement.dto.response.MediaResponse;
import com.example.coursemanagement.dto.response.StudentResponse;
import com.example.coursemanagement.entity.Image;
import com.example.coursemanagement.entity.Student;
import com.example.coursemanagement.enums.FileType;
import com.example.coursemanagement.enums.ObjectType;
import com.example.coursemanagement.exception.DuplicateException;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.mapper.ImageMapper;
import com.example.coursemanagement.mapper.StudentMapper;
import com.example.coursemanagement.repository.ImageRepository;
import com.example.coursemanagement.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final ImageMapper imageMapper;
    private final MessageSource messageSource;
    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    public StudentService(StudentRepository studentRepository, CloudinaryService cloudinaryService, MessageSource messageSource, ImageRepository imageRepository, StudentMapper studentMapper, ImageMapper imageMapper) {
        this.studentRepository = studentRepository;
        this.cloudinaryService = cloudinaryService;
        this.messageSource = messageSource;
        this.imageRepository = imageRepository;
        this.studentMapper = studentMapper;
        this.imageMapper = imageMapper;
    }


    public Page<StudentResponse> getAllStudents(StudentSearchRequest request) {
        Sort sort = Sort.by(
                request.getSortDirection().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );

        String keyword = request.getKeyword() != null ? escapeLike(request.getKeyword()) : null;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Student> studentPage = studentRepository.searchStudents(keyword, "1", request.getGender(), pageable);
        List<Student> students = new ArrayList<>(studentPage.getContent());

        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();

        List<Image> images = imageRepository.findAllActiveByObjectAndType(
                ObjectType.student,
                studentIds,
                FileType.avatar,
                "1"
        );

        // map 1 student - image list (avatar)
        Map<Long, List<Image>> avatarMap = images.stream().collect(Collectors.groupingBy(Image::getObjectId, Collectors.toList()));

        List<StudentResponse> response = studentMapper.toResponseList(students);
        response.forEach(
                st -> {
                    List<Image> avatars = avatarMap.getOrDefault(st.getId(), List.of());
                    if (!avatars.isEmpty()) {
                        List<MediaResponse> mediaResponses = imageMapper.toResponseList(avatars);
                        st.setAvatar(mediaResponses);
                    } else {
                        st.setAvatar(null); 
                    }
                }
        );

        return new PageImpl<>(response, pageable, studentPage.getTotalElements());
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        List<String> errors = new ArrayList<>();

        if (studentRepository.findByEmailAndActive(request.getEmail(), "1").isPresent()) {
            errors.add("student.email.duplicate");
        }

        if (studentRepository.findByPhoneAndActive(request.getPhone(), "1").isPresent()) {
            errors.add("student.phone.duplicate");
        }

        if (!errors.isEmpty()) {
            throw new DuplicateException(errors);
        }

        Student student = studentMapper.toEntity(request);

        Student savedStudent = studentRepository.save(student);

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            List<Image> avatarImages = uploadAvatar(savedStudent.getId(), request.getAvatar());
            savedStudent.getImages().addAll(avatarImages);
            StudentResponse response = studentMapper.toResponse(savedStudent);
            List<MediaResponse> mediaResponses = imageMapper.toResponseList(avatarImages);
            response.setAvatar(mediaResponses);
            return response;
        }
        return studentMapper.toResponse(savedStudent);
    }

    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findActiveById(id, "1").orElseThrow(
                () -> new NotFoundException("student.not.found")
        );
        StudentResponse response = studentMapper.toResponse(student);
        List<Image> avatar = imageRepository.findActiveByObjectAndType(ObjectType.student, student.getId(), FileType.avatar, "1");
        List<MediaResponse> mediaResponses = imageMapper.toResponseList(avatar);
        response.setAvatar(mediaResponses);
        return response;
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student studentExist = studentRepository.findActiveById(id, "1")
                .orElseThrow(() -> new NotFoundException("student.not.found"));
        List<String> errors = new ArrayList<>();

        if (!Objects.equals(request.getEmail(), studentExist.getEmail())) {
            if (studentRepository.findByEmailAndActive(request.getEmail(), "1").isPresent()) {
                errors.add("student.email.duplicate");
            }
            studentExist.setEmail(request.getEmail());
        }
        if (!Objects.equals(request.getPhone(), studentExist.getPhone())) {
            if (studentRepository.findByPhoneAndActive(request.getPhone(), "1").isPresent()) {
                errors.add("student.phone.duplicate");
            }
            studentExist.setPhone(request.getPhone());
        }
        if (!errors.isEmpty()) {
            throw new DuplicateException(errors);
        }

        // request -> entity
        studentMapper.updateEntity(request, studentExist);

        // soft delete
        if (request.getDeleteAvatarIds() != null && !request.getDeleteAvatarIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllByIdInAndStatus(request.getDeleteAvatarIds(), "1");
            imagesToDelete.forEach(img -> img.setStatus("0"));
            studentExist.getImages().addAll(imagesToDelete);
        }

        // if there is a new images uploaded
        if (request.getAvatar() != null) {
                List<Image> uploadedImages = uploadAvatar(studentExist.getId(), request.getAvatar());
                studentExist.getImages().addAll(uploadedImages);
        }
        Student updatedStudent = studentRepository.save(studentExist);
        StudentResponse response = studentMapper.toResponse(updatedStudent);
        List<MediaResponse> mediaResponses = imageMapper.toResponseList(
                imageRepository.findActiveByObjectAndType(ObjectType.student, updatedStudent.getId(), FileType.avatar, "1")
        );
        response.setAvatar(mediaResponses);
        return response;
    }

    @Transactional
    public String softDeleteStudent(Long id) {
        Student student = studentRepository.findActiveById(id, "1")
                .orElseThrow(() -> new NotFoundException("student.not.found"));
        student.setStatus("0");
        student.setUpdatedAt(new Date(System.currentTimeMillis()));
        studentRepository.save(student);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("student.deleted", null, locale);
    }

    private String escapeLike(String keyword) {
        if (keyword == null) return null;
        return keyword
                .replace("\\", "\\\\")   // escape backslash
                .replace("%", "\\%")
                .replace("_", "\\_");
    }


    @Transactional
    public byte[] exportAllStudentsToExcel() throws IOException {

        // Lấy tất cả học sinh có status = 1 (active)
        List<Student> students = studentRepository.findAllByStatus("1");

        // Tạo workbook Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Tạo định dạng cho các cột ngày tháng
        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        // Tạo row header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Gender", "Email", "Phone", "Status", "Created at", "Updated at"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Điền dữ liệu vào các row tiếp theo
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getName());
            row.createCell(2).setCellValue(student.getGender().equals("1") ? "Nam" : "Nữ");
            row.createCell(3).setCellValue(student.getEmail());
            row.createCell(4).setCellValue(student.getPhone());
            row.createCell(5).setCellValue(student.getStatus());

            // Áp dụng định dạng ngày giờ cho các cột "Created at" và "Updated at"
            Cell createdAtCell = row.createCell(6);
            createdAtCell.setCellValue(student.getCreatedAt());
            createdAtCell.setCellStyle(dateCellStyle);  // Áp dụng định dạng

            Cell updatedAtCell = row.createCell(7);
            updatedAtCell.setCellValue(student.getUpdatedAt());
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


    private List<Image> uploadAvatar(Long studentId, List<MultipartFile> files) {
        // get max orderIndex
        Integer maxOrder = imageRepository.findMaxOrderIndex(
                ObjectType.student,
                studentId,
                FileType.avatar
        );

        System.out.println("maxOrder: " + maxOrder);
        List<Image> images = new ArrayList<>();
        int index = maxOrder;

        for (MultipartFile file : files) {
            Map<String, String> uploadResult = cloudinaryService.uploadFile(file);
            Image image = Image.builder()
                    .objectType(ObjectType.student)
                    .objectId(studentId)
                    .url(uploadResult.get("url"))
                    .publicId(uploadResult.get("public_id"))
                    .fileType(FileType.avatar)
                    .orderIndex(++index)
                    .status("1")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .build();
            images.add(image);
        }
        return images;
    }

    public List<StudentResponse> getSelectStudents() {
        return studentMapper.toResponseList(studentRepository.findAllByStatus("1"));
    }


//    private void deleteOldAvatar(Long studentId) {
//        imageRepository.findActiveByObjectAndType(
//                ObjectType.student,
//                studentId,
//                FileType.avatar
//        ).ifPresent(image -> {
//            // Delete from Cloudinary
//            cloudinaryService.deleteFile(image.getPublicId(), "avatar");
//            // Delete from DB
//            imageRepository.delete(image);
//        });
//    }
//
//    private String getAvatarUrl(Long studentId) {
//        return imageRepository.findActiveByObjectAndType(
//                ObjectType.student,
//                studentId,
//                FileType.avatar
//        ).map(Image::getUrl).orElse(null);
//    }
}
