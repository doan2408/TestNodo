package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByStatus(String status);

    @Query("SELECT c FROM Course c WHERE c.id = :id AND c.status = :status")
    Optional<Course> findActiveById(@Param("id") Long id, @Param("status") String status);

    @Query("SELECT c FROM Course c WHERE c.code = :code AND c.status = :status")
    Optional<Course> findByCodeAndActive(@Param("code") String code,
                                         @Param("status") String status
                                         );

    @Query("SELECT c FROM Course c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\')")
    Page<Course> searchCourses(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = '1'")
    int countActiveEnrollments(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId AND l.status = '1'")
    int countActiveLessons(@Param("courseId") Long courseId);
}
