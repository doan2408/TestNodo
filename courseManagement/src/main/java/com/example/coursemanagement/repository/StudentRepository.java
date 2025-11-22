package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.images i WHERE s.id = :id AND s.status = :status")
    Optional<Student> findActiveById(@Param("id") Long id, @Param("status") String status);

    @Query("SELECT s FROM Student s WHERE s.email = :email AND s.status = :status")
    Optional<Student> findByEmailAndActive(@Param("email") String email,
                                           @Param("status") String status
    );

    List<Student> findAllByStatus(String status);

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    @Query("SELECT s FROM Student s WHERE s.phone = :phone AND s.status = :status")
    Optional<Student> findByPhoneAndActive(@Param("phone") String phone,
                                           @Param("status") String status
    );

    @Query("SELECT s FROM Student s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "s.phone LIKE CONCAT('%', :keyword, '%') ESCAPE '\\') AND " +
           "(:gender IS NULL OR :gender = '' or s.gender = :gender)")
    Page<Student> searchStudents(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("gender") String gender,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = '1'")
    int countActiveEnrollments(@Param("studentId") Long studentId);


}
