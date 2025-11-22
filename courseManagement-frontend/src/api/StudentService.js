import api from "./axios";

const baseURL = "/api/student";

export const getSelectStudents = async () => {
    try {
        const response = await api.get(`${baseURL}/selection`);
        return response.data;
    }
    catch (err) {
        console.log("Error when get all student: ", err);
        throw err;
    }
}

// Lấy danh sách sinh viên (có phân trang + tìm kiếm)
export const getAllStudent = async ({
    keyword = "",
    gender = "",
    page = 0,
    size = 10,
    sortBy = "id",
    sortDirection = "DESC"
} = {}) => {
    try {
        const response = await api.get(baseURL, {
            params: {
                keyword,
                gender,
                page,
                size,
                sortBy,
                sortDirection
            },
            // withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error("thrown error when loading student: ", error);
        // Rethrow để component caller có thể xử lý lỗi (tránh trả undefined)
        throw error;
    }
};

// Lấy 1 sinh viên theo ID
export const getStudentById = async (id) => {
    try {
        const response = await api.get(`${baseURL}/${id}`, { withCredentials: true });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Tạo sinh viên (form-data)
export const createStudent = async (formData) => {
    try {
        const response = await api.post(baseURL, formData, {
            headers: { "Content-Type": "multipart/form-data" },
            // withCredentials: true
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Cập nhật sinh viên
export const updateStudent = async (id, formData) => {
    try {
        const response = await api.put(`${baseURL}/${id}`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
            // withCredentials: true
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Xóa mềm sinh viên
export const deleteStudent = async (id) => {
    try {
        const response = await api.delete(`${baseURL}/${id}`,
             { withCredentials: true });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Export Excel
export const exportStudents = async () => {
    try {
        const response = await api.get(`${baseURL}/export`, {
            responseType: 'blob',
            // withCredentials: true
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};
