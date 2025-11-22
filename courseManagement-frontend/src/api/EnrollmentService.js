import api from "./axios";

const baseURL = "/api/enrollment";

// 1. Lấy danh sách sinh viên theo courseId
export const getStudentsByCourse = async (courseId) => {
    try {
        const response = await api.get(`${baseURL}/course/${courseId}`);
        return response.data; // Trả về danh sách StudentResponse[]
    } catch (error) {
        console.error("Error loading students by course:", error);
        throw error;
    }
};

// 2. Lấy danh sách khóa học theo studentId
export const getEnrollmentsByStudent = async (studentId) => {
    try {
        const response = await api.get(`${baseURL}/student/${studentId}`);
        return response.data; // Trả về danh sách EnrollmentResponse[]
    } catch (error) {
        console.error("Error loading enrollments by student:", error);
        throw error;
    }
};

// 3. Đăng ký sinh viên vào nhiều khóa học cùng lúc
export const enrollStudent = async (enrollmentRequest) => {
    try {
        const response = await api.post(baseURL, enrollmentRequest);
        return response.data; // Trả về EnrollmentBatchResponse
    } catch (error) {
        console.error("Error enrolling student:", error);
        throw error;
    }
};

// 4. Cập nhật đăng ký của sinh viên (overwrite tất cả enrollments)
export const updateEnrollment = async (enrollmentRequest) => {
    try {
        const response = await api.put(baseURL, enrollmentRequest);
        return response.data; // Trả về EnrollmentBatchResponse
    } catch (error) {
        console.error("Error updating enrollment:", error);
        throw error;
    }
};

// 5. Xóa đăng ký (soft delete)
export const deleteEnrollment = async (studentId, courseId) => {
    try {
        const response = await api.delete(`${baseURL}/student/${studentId}/course/${courseId}`);
        return response.data; // Trả về thông báo success
    } catch (error) {
        console.error("Error deleting enrollment:", error);
        throw error;
    }
};
