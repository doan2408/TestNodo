import api from "./axios";

const baseURL = "/api/lesson";

// Lấy danh sách bài học theo courseId (có phân trang + tìm kiếm)
export const getAllLessons = async ({
    courseId = null,
    keyword = "",
    page = 0,
    size = 10,
    sortBy = "id",
    sortDirection = "DESC"
} = {}) => {
    try {
        // courseId là path variable: GET /api/lesson/{courseId}
        const response = await api.get(`${baseURL}/${courseId}`, {
            params: {
                keyword,
                page,
                size,
                sortBy,
                sortDirection
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error loading lessons:", error);
        throw error;
    }
};

// Lấy chi tiết bài học theo lessonId
export const getLessonDetail = async (lessonId) => {
    try {
        const response = await api.get(`${baseURL}/detail/${lessonId}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Tạo bài học (form-data)
export const createLesson = async (formData) => {
    try {
        const response = await api.post(baseURL, formData, {
            headers: { "Content-Type": "multipart/form-data" }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Cập nhật bài học (form-data)
export const updateLesson = async (id, formData) => {
    try {
        const response = await api.put(`${baseURL}/${id}`, formData, {
            headers: { "Content-Type": "multipart/form-data" }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Xóa bài học (soft delete)
export const deleteLesson = async (id) => {
    try {
        const response = await api.delete(`${baseURL}/${id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};
