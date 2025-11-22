package com.example.coursemanagement.repository;

import com.example.coursemanagement.enums.FileType;
import com.example.coursemanagement.enums.ObjectType;
import com.example.coursemanagement.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.objectType = :objectType AND i.objectId IN :objectIds AND i.fileType = :fileType AND i.status = :status")
    List<Image> findAllActiveByObjectAndType(@Param("objectType") ObjectType objectType, @Param("objectIds") List<Long> objectIds, @Param("fileType") FileType fileType, @Param("status") String status);

    @Query("SELECT i FROM Image i WHERE i.objectType = :objectType AND i.objectId = :objectId AND i.fileType = :fileType AND i.status = :status")
    List<Image> findActiveByObjectAndType(@Param("objectType") ObjectType objectType, @Param("objectId") Long objectId, @Param("fileType") FileType fileType, @Param("status") String status);

    @Query("SELECT i FROM Image i WHERE i.objectType = :objectType AND i.objectId = :objectId AND i.status = :status")
    List<Image> findAllActiveByObject(@Param("objectType") ObjectType objectType, @Param("objectId") Long objectId, @Param("status") String status);

    // get maxOrder
    @Query("SELECT COALESCE(MAX(i.orderIndex), 0) from Image i " + "WHERE i.objectType = :objectType " + "AND i.objectId = :objectId " + "AND i.fileType = :fileType")
    Integer findMaxOrderIndex(@Param("objectType") ObjectType objectType, @Param("objectId") Long objectId, @Param("fileType") FileType fileType);

    // search active images to deleted soft by list of image id
    List<Image> findAllByIdInAndStatus(List<Long> ids, String status);
}
