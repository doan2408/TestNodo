<script setup>
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useLocale } from '@/composables/useLocale';

const router = useRouter();
const route = useRoute();

const { locale, setLocale } = useLocale();

const activeLink = ref('');

watch(
  () => route.path,
  (newPath) => {
    activeLink.value = newPath;
  },
  { immediate: true }
);
</script>

<template>
  <div class="header-container">
    <div class="logo">
      <h2>{{ $t('course.title') }}</h2>
    </div>

    <div class="nav-links">
      <el-menu :default-active="activeLink" mode="horizontal">
        <el-menu-item index="/students" @click="router.push('/students')">
          {{ $t('student.title') }}
        </el-menu-item>

        <el-menu-item index="/courses" @click="router.push('/courses')">
          {{ $t('course.title') }}
        </el-menu-item>

        <el-menu-item index="/enrollments" @click="router.push('/enrollments')">
          Đăng ký
        </el-menu-item>
      </el-menu>
    </div>

    <div class="language-toggle">
      <!-- Nút đổi ngôn ngữ: đồng bộ UI + backend -->
      <el-switch
        v-model="locale"
        active-value="en"
        inactive-value="vi"
        active-text="EN"
        inactive-text="VI"
        @change="setLocale"
      />
    </div>
  </div>
</template>



<style scoped>
.header-wrapper {
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.5);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01), 0 2px 4px -1px rgba(0, 0, 0, 0.01);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #6366f1 0%, #3b82f6 100%);
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.custom-menu {
  border-bottom: none !important;
  background: transparent !important;
  height: 48px;
  display: flex;
  align-items: center;
  padding: 4px;
  border-radius: 9999px;
  background-color: rgba(243, 244, 246, 0.5) !important; /* Slate-100 with opacity */
}

:deep(.el-menu-item) {
  height: 40px;
  line-height: 40px;
  border: none !important;
  border-radius: 9999px;
  margin: 0 2px;
  color: #64748b;
  font-weight: 500;
  font-size: 0.95rem;
  padding: 0 20px !important;
  transition: all 0.2s ease;
}

:deep(.el-menu-item:hover) {
  color: #3b82f6 !important;
  background-color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #ffffff !important;
  color: #3b82f6 !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  font-weight: 600;
}

/* Switch Styling */
:deep(.el-switch.lang-switch .el-switch__core) {
  border-color: #e2e8f0;
  background-color: #f1f5f9;
}

:deep(.el-switch.lang-switch.is-checked .el-switch__core) {
  border-color: #6366f1;
  background-color: #6366f1;
}

:deep(.el-switch__label) {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
}

:deep(.el-switch__label.is-active) {
  color: #6366f1;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo-text {
    font-size: 1.25rem;
  }
  
  :deep(.el-menu-item) {
    padding: 0 12px !important;
    font-size: 0.9rem;
  }
}
</style>