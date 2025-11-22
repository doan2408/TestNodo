package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.request.EnrollmentRequest;
import com.example.coursemanagement.dto.response.*;
import com.example.coursemanagement.entity.*;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.mapper.EnrollmentMapper;
import com.example.coursemanagement.mapper.StudentMapper;
import com.example.coursemanagement.repository.CourseRepository;
import com.example.coursemanagement.repository.EnrollmentRepository;
import com.example.coursemanagement.repository.ImageRepository;
import com.example.coursemanagement.repository.StudentRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final MessageSource messageSource;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, MessageSource messageSource, StudentRepository studentRepository, CourseRepository courseRepository, StudentMapper studentMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.messageSource = messageSource;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studentMapper = studentMapper;
    }

    @Transactional
    public EnrollmentBatchResponse enrollStudentToCourses(EnrollmentRequest request) {
        // Validate student exists
        Student student = studentRepository.findActiveById(request.getStudentId(), "1")
                .orElseThrow(() -> new NotFoundException("student.not.found"));

        List<CourseEnrollmentInfo> courseInfos = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        // Đăng ký từng khóa học
        for (Long courseId : request.getCourseIds()) {
            try {
                // Validate course exists
                Course course = courseRepository.findActiveById(courseId, "1")
                        .orElseThrow(() -> new NotFoundException("course.not.found"));

                // Check duplicate enrollment
                if (enrollmentRepository.existsByStudentAndCourseAndActive(
                        request.getStudentId(), courseId, "1")) {
                    courseInfos.add(CourseEnrollmentInfo.builder()
                            .courseId(courseId)
                            .courseCode(course.getCode())
                            .courseName(course.getName())
                            .success(false)
                            .message("enrollment.already.exists")
                            .build());
                    failCount++;
                    continue;
                }

                // Create enrollment
                Enrollment enrollment = Enrollment.builder()
                        .id(new EnrollmentId(request.getStudentId(), courseId))
                        .student(student)
                        .course(course)
                        .status("1")
                        .createdAt(new Date(System.currentTimeMillis()))
                        .updatedAt(new Date(System.currentTimeMillis()))
                        .build();
                enrollmentRepository.save(enrollment);

                courseInfos.add(CourseEnrollmentInfo.builder()
                        .courseId(courseId)
                        .courseCode(course.getCode())
                        .courseName(course.getName())
                        .success(true)
                        .message("enrollment.created")
                        .build());
                successCount++;

            } catch (Exception e) {
                courseInfos.add(CourseEnrollmentInfo.builder()
                        .courseId(courseId)
                        .success(false)
                        .message(e.getMessage())
                        .build());
                failCount++;
            }
        }

        return EnrollmentBatchResponse.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .courses(courseInfos)
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }


    public List<StudentResponse> getStudentsByCourse(Long courseId) {
        // Validate course exists
        courseRepository.findActiveById(courseId, "1")
                .orElseThrow(() -> new NotFoundException("course.not.found"));
        List<Student> students = enrollmentRepository.findStudentsByCourseIdAndStatus(courseId, "1");

        return studentMapper.toResponseList(students);
    }

//    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
//        // Validate student exists
//        Student student = studentRepository.findActiveById(studentId, "1")
//                .orElseThrow(() -> new NotFoundException("student.not.found"));
//
//        // Get all enrollments for this student
//        List<Enrollment> enrollments = enrollmentRepository.findAllByStudentIdAndActive(studentId, "1");
//
//        // get all courseIds
//        Set<Long> courseIds = enrollments.stream()
//                .map(enrollment -> enrollment.getCourse().getId())
//                .collect(Collectors.toSet());
//
//        // fetch all courses at once
//        List<Course> courses = courseRepository.findAllById(courseIds);
//
//        // map courseId -> Course
//        Map<Long, Course> courseMap = courses.stream()
//                .collect(Collectors.toMap(Course::getId, c -> c));
//
//        return enrollments.stream()
//                .map(enrollment -> {
//                    Course course = courseMap.getOrDefault(enrollment.getCourse().getId(), null);
//                    CourseEnrollmentInfo courseInfo = new CourseEnrollmentInfo(
//                            course.getId(),
//                            course.getCode(),
//                            course.getName(),
//                            true,
//                            messageSource.getMessage("enrollment.updated", null, LocaleContextHolder.getLocale())
//                    );
//                    return EnrollmentResponse.builder()
//                            .studentId(student.getId())
//                            .studentName(student.getName())
//                            .studentEmail(student.getEmail())
//                            .studentPhone(student.getPhone())
//                            .gender(student.getGender())
//                            .courses(List.of(courseInfo))
//                            .status(enrollment.getStatus())
//                            .createdAt(enrollment.getCreatedAt())
//                            .updatedAt(enrollment.getUpdatedAt())
//                            .build();
//                }).toList();
//    }

    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        // Validate student exists
        Student student = studentRepository.findActiveById(studentId, "1")
                .orElseThrow(() -> new NotFoundException("student.not.found"));

        // Get all enrollments for this student
        List<Enrollment> enrollments = enrollmentRepository.findAllByStudentIdAndActive(studentId, "1");

        // get all courseIds
        Set<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());

        // fetch all courses at once
        List<Course> courses = courseRepository.findAllById(courseIds);

        // map courseId -> Course
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // Group enrollments by studentId and aggregate course information
        Map<Long, EnrollmentResponse> responseMap = new HashMap<>();

        for (Enrollment enrollment : enrollments) {
            // Get the course info
            Course course = courseMap.getOrDefault(enrollment.getCourse().getId(), null);
            CourseEnrollmentInfo courseInfo = new CourseEnrollmentInfo(
                    course.getId(),
                    course.getCode(),
                    course.getName(),
                    true,
                    messageSource.getMessage("enrollment.updated", null, LocaleContextHolder.getLocale())
            );

            // Check if student is already in the response map
            responseMap.computeIfAbsent(studentId, id -> EnrollmentResponse.builder()
                            .studentId(student.getId())
                            .studentName(student.getName())
                            .studentEmail(student.getEmail())
                            .studentPhone(student.getPhone())
                            .gender(student.getGender())
                            .status(enrollment.getStatus())
                            .createdAt(enrollment.getCreatedAt())
                            .updatedAt(enrollment.getUpdatedAt())
                            .courses(new ArrayList<>()) // Initialize empty list of courses
                            .build())
                    .getCourses().add(courseInfo); // Add course info to the list
        }

        // Return the response map values as a list
        return new ArrayList<>(responseMap.values());
    }



    @Transactional
    public EnrollmentBatchResponse updateEnrollments(EnrollmentRequest request) {
        // Validate student exists
        Student student = studentRepository.findActiveById(request.getStudentId(), "1")
                .orElseThrow(() -> new NotFoundException("student.not.found"));

        // Lấy tất cả enrollment hiện tại của student
        List<Enrollment> currentEnrollments = enrollmentRepository.findAllByStudentIdAndActive(
                request.getStudentId(), "1");

        // Soft delete tất cả enrollments cũ
        for (Enrollment enrollment : currentEnrollments) {
            enrollment.setStatus("0");
            enrollmentRepository.save(enrollment);
        }

        // Tạo lại enrollments mới
        List<CourseEnrollmentInfo> courseInfos = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Long courseId : request.getCourseIds()) {
            try {
                // Validate course exists
                Course course = courseRepository.findActiveById(courseId, "1")
                        .orElseThrow(() -> new NotFoundException("course.not.found"));

                // Create new enrollment
                Enrollment enrollment = Enrollment.builder()
                        .id(new EnrollmentId(request.getStudentId(), courseId))
                        .student(student)
                        .course(course)
                        .status("1")
                        .updatedAt(new Date(System.currentTimeMillis()))
                        .build();
                enrollmentRepository.save(enrollment);

                courseInfos.add(CourseEnrollmentInfo.builder()
                        .courseId(courseId)
                        .courseCode(course.getCode())
                        .courseName(course.getName())
                        .success(true)
                        .message(messageSource.getMessage("enrollment.created", null, LocaleContextHolder.getLocale()))
                        .build());
                successCount++;

            } catch (Exception e) {
                courseInfos.add(CourseEnrollmentInfo.builder()
                        .courseId(courseId)
                        .success(false)
                        .message(e.getMessage())
                        .build());
                failCount++;
            }
        }

        return EnrollmentBatchResponse.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .courses(courseInfos)
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }


    public String deleteEnrollment(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findActiveByStudentAndCourse(studentId, courseId, "1")
                .orElseThrow(() -> new NotFoundException("enrollment.not.found"));
        // Soft delete
        enrollment.setStatus("0");
        enrollmentRepository.save(enrollment);
        Locale local = LocaleContextHolder.getLocale();
        return messageSource.getMessage("enrollment.deleted", null, local);
    }


}
