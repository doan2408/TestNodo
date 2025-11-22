package com.example.coursemanagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    private Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload file lên Cloudinary
     */
    public Map<String, String> uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto", // images, video, file
                            "folder", "course_management" // link in cloudinary
                    )
            );

            Map<String, String> result = new HashMap<>();
            result.put("url", uploadResult.get("secure_url").toString());
            result.put("public_id", uploadResult.get("public_id").toString());
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file - tự động xác định resource_type
     */
    public void deleteFile(String publicId, String fileType) {
        try {
            // Convert file_type → resource_type
            String resourceType = getResourceType(fileType);

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
        } catch (IOException e) {
            throw new RuntimeException("Xóa file thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: Convert file_type (DB) → resource_type (Cloudinary)
     */
    private String getResourceType(String fileType) {
        if (fileType.equals("video")) {
            return "video";
        } else {
            return "image"; // avatar, thumbnail
        }
    }
}
