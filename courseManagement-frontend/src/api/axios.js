import axios from 'axios';

// Tạo axios instance
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

// Request interceptor
api.interceptors.request.use(
    (config) => {
        const language = localStorage.getItem('language') || 'vi'; // Đọc locale từ localStorage
        config.headers['Accept-Language'] = language; // Gửi locale theo header
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
    (response) => {
        console.log('✅ Response:', response.status, response.data);
        return response;
    },
    (error) => {
        console.error('❌ Response Error:', error.response?.status, error.message);
        return Promise.reject(error);
    }
);

export default api;