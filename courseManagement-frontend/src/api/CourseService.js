import api from "./axios";

const baseURL = "/api/course";

export const getSelectCourses = async () => {
    try {
        const response = await api.get(`${baseURL}/selection`);
        return response.data;
    }
    catch (err) {
        console.log("Lỗi khi get all course", err);
        throw err;
    }
}


// Lấy danh sách khóa học (có phân trang + tìm kiếm)
export const getAllCourses = async ({
    keyword = "",
    page = 0,
    size = 10,
    sortBy = "id",
    sortDirection = "DESC"
} = {}) => {
    try {
        const response = await api.get(baseURL, {
            params: {
                keyword,
                page,
                size,
                sortBy,
                sortDirection
            },
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error("Thao tác lấy danh sách khóa học bị lỗi: ", error);
        throw error;
    }
};

// Lấy thông tin 1 khóa học theo ID
export const getCourseById = async (id) => {
    try {
        const response = await api.get(`${baseURL}/${id}`, { withCredentials: true });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi lấy khóa học theo ID:", error);
        throw error;
    }
};

// Tạo mới khóa học (form-data)
export const createCourse = async (formData) => {
    try {
        const response = await api.post(baseURL, formData, {
            headers: { "Content-Type": "multipart/form-data" },
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi tạo khóa học:", error);
        throw error;
    }
};

// Cập nhật thông tin khóa học
export const updateCourse = async (id, formData) => {
    try {
        const response = await api.put(`${baseURL}/${id}`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi cập nhật khóa học:", error);
        throw error;
    }
};

// Xóa mềm khóa học
export const deleteCourse = async (id) => {
    try {
        const response = await api.delete(`${baseURL}/${id}`, { withCredentials: true });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi xóa khóa học:", error);
        throw error;
    }
};

// Export danh sách khóa học ra Excel
export const exportCourses = async () => {
    try {
        const response = await api.get(`${baseURL}/export`, {
            responseType: 'blob',
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi xuất khóa học:", error);
        throw error;
    }
};
