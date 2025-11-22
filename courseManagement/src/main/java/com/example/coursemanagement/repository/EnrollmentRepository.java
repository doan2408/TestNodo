package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.Enrollment;
import com.example.coursemanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT e FROM Enrollment e WHERE e.id.studentId = :studentId AND e.id.courseId = :courseId AND e.status = :status")
    Optional<Enrollment> findActiveByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("status") String status);

    @Query("SELECT e FROM Enrollment e WHERE e.id.studentId = :studentId AND e.status = :status")
    List<Enrollment> findAllByStudentIdAndActive(@Param("studentId") Long studentId, @Param("status") String status);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e " + "WHERE e.id.studentId = :studentId AND e.id.courseId = :courseId AND e.status = :status")
    boolean existsByStudentAndCourseAndActive(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("status") String status);

    @Query("SELECT e.student FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status")
    List<Student> findStudentsByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") String status);

}
