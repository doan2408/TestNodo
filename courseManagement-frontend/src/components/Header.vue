<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRouter as getRouter, useRoute as getRoute } from 'vue-router';


// Router instance
const router = getRouter();
const route = getRoute();

// State to track current language
const language = ref('vi'); // Default is Vietnamese

// Ref to store the active link
const activeLink = ref('');

// Change language and set to the header
const setLanguage = (lang) => {
  language.value = lang;
  localStorage.setItem('language', lang);
};

// Go to page based on selected route
const goToPage = (path) => {
  router.push(path);
  // activeLink is handled by the watcher
};

// Sync active link with current route
watch(
  () => route.path,
  (newPath) => {
    activeLink.value = newPath;
  },
  { immediate: true }
);



onMounted(() => {
  const savedLang = localStorage.getItem('language');
  if (savedLang) {
    language.value = savedLang;
  }
});
</script>

<template>
  <div class="header-container">
    <div class="logo">
      <h2>Quản lý Khóa Học</h2>
    </div>
    <div class="nav-links">
      <el-menu mode="horizontal" :default-active="activeLink" class="menu" :ellipsis="false">
        <el-menu-item index="/students" @click="goToPage('/students')">
          Sinh viên
        </el-menu-item>
        <el-menu-item index="/courses" @click="goToPage('/courses')">
          Khóa học
        </el-menu-item>
        <!-- <el-menu-item index="/lessons" @click="goToPage('/lessons')">
          Bài học
        </el-menu-item> -->
        <el-menu-item index="/enrollments" @click="goToPage('/enrollments')">
          Đăng ký
        </el-menu-item>
      </el-menu>
    </div>

    <!-- Language Toggle -->
    <div class="language-toggle">
      <el-switch
        v-model="language"
        active-value="en"
        inactive-value="vi"
        active-text="EN"
        inactive-text="VI"
        inline-prompt
        style="--el-switch-on-color: #4f46e5; --el-switch-off-color: #10b981"
        @change="setLanguage"
      />
    </div>
  </div>
</template>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  height: 70px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #4f46e5;
  margin: 0;
  letter-spacing: -0.5px;
}

.nav-links {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu {
  border-bottom: none !important;
  background: transparent;
  height: 70px;
  display: flex;
  align-items: center;
}

:deep(.el-menu-item) {
  font-size: 15px;
  font-weight: 500;
  color: #6b7280;
  height: 70px;
  line-height: 70px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  color: #4f46e5 !important;
  background-color: transparent !important;
}

:deep(.el-menu-item.is-active) {
  color: #4f46e5 !important;
  border-bottom: 2px solid #4f46e5 !important;
  background-color: transparent !important;
}

.language-toggle {
  display: flex;
  align-items: center;
}

/* Custom switch styling */
:deep(.el-switch__core) {
  border-radius: 20px;
  border: 1px solid #e5e7eb;
}

:deep(.el-switch__label) {
  font-weight: 600;
  font-size: 12px;
}
</style>
