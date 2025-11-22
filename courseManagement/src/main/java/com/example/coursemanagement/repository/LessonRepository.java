package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l WHERE l.id = :id AND l.status = :status")
    Optional<Lesson> findActiveById(@Param("id") Long id, @Param("status") String status);

    // get maxOrder of lesson by course
//    @Query("SELECT COALESCE(max(l.orderIndex),0) from Lesson l " +
//           "WHERE l.course.id = :courseId and l.status = :status")
//    Integer findMaxOrderIndex(@Param("courseId") Long courseId, @Param("status") String status);

    @Query("""
    SELECT l FROM Lesson l WHERE
     (:courseId IS NULL OR l.course.id = :courseId) AND
     (:status IS NULL OR l.status = :status) AND
     (:keyword IS NULL OR :keyword = '' OR
      LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\')
    """)
    Page<Lesson> searchLesson(@Param("courseId") Long courseId,
                              @Param("status") String status,
                              @Param("keyword") String keyword,
                              Pageable pageable);




    Optional<Lesson> findByTitleAndStatus(String title, String status);
}
