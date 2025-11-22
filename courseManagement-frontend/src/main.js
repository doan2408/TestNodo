import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import i18n from './i18n'
import axiosInstance from '@/api/axios'
import { watch } from 'vue'

// import axiosInstance from './api/axios'
axios.defaults.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
axios.defaults.withCredentials = true;

const app = createApp(App)
// Nếu muốn dùng axios global trong Option API: this.$axios
// app.config.globalProperties.$axios = axiosInstance
// đồng bộ header Accept-Language với i18n hiện tại
axiosInstance.defaults.headers['Accept-Language'] = i18n.global.locale.value || 'vi'

// watch thay đổi locale để cập nhật header
watch(() => i18n.global.locale.value, (val) => {
  axiosInstance.defaults.headers['Accept-Language'] = val
})

app.use(ElementPlus)
app.use(i18n)
app.use(router)

app.mount('#app');
// createApp(App).mount('#app'
// Đăng ký icon Element Plus
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
