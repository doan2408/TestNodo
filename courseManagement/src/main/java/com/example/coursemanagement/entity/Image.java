package com.example.coursemanagement.entity;

import com.example.coursemanagement.enums.FileType;
import com.example.coursemanagement.enums.ObjectType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;

@Builder
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    @NotNull
    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Size(max = 500)
    @NotNull
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Size(max = 255)
    @Column(name = "public_id")
    private String publicId;

    @NotNull
    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @ColumnDefault("'1'")
    @Lob
    @Column(name = "status", columnDefinition = "ENUM('1', '0') DEFAULT '1'")
    private String status;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_at")
    private Date createdAt;

    public Image() {
    }

    public Image(Long id, ObjectType objectType, Long objectId, String url, String publicId, FileType fileType, Integer orderIndex, String status, Date createdAt) {
        this.id = id;
        this.objectType = objectType;
        this.objectId = objectId;
        this.url = url;
        this.publicId = publicId;
        this.fileType = fileType;
        this.orderIndex = orderIndex;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(@NotNull ObjectType objectType) {
        this.objectType = objectType;
    }

    public @NotNull Long getObjectId() {
        return objectId;
    }

    public void setObjectId(@NotNull Long objectId) {
        this.objectId = objectId;
    }

    public @Size(max = 500) @NotNull String getUrl() {
        return url;
    }

    public void setUrl(@Size(max = 500) @NotNull String url) {
        this.url = url;
    }

    public @Size(max = 255) String getPublicId() {
        return publicId;
    }

    public void setPublicId(@Size(max = 255) String publicId) {
        this.publicId = publicId;
    }

    public @NotNull FileType getFileType() {
        return fileType;
    }

    public void setFileType(@NotNull FileType fileType) {
        this.fileType = fileType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}