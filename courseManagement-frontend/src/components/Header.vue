<script setup>
import { ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useLocale } from "@/composables/useLocale";

const router = useRouter();
const route = useRoute();

const { locale, setLocale } = useLocale();

const activeLink = ref("");

watch(
  () => route.path,
  (newPath) => {
    activeLink.value = newPath;
  },
  { immediate: true }
);
</script>

<template>
  <!-- Header wrapper (sticky + background blur) -->
  <header class="header-wrapper">
    <!-- Content container (max-width 1200px + padding) -->
    <div class="header-content">
      <!-- Left: Logo -->
      <div class="logo">
        <h2 class="logo-text">{{ $t("course.title") }}</h2>
      </div>

      <!-- Center: Menu -->
      <nav class="nav-section">
        <el-menu
          :default-active="activeLink"
          mode="horizontal"
          class="custom-menu"
        >
          <el-menu-item index="/students" @click="router.push('/students')">
            {{ $t("student.title") }}
          </el-menu-item>

          <el-menu-item index="/courses" @click="router.push('/courses')">
            {{ $t("course.title") }}
          </el-menu-item>

          <el-menu-item
            index="/enrollments"
            @click="router.push('/enrollments')"
          >
            {{ $t("enrollment.title") }}
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- Right: Language switch -->
      <div class="language-toggle">
        <el-switch
          class="lang-switch"
          v-model="locale"
          active-value="en"
          inactive-value="vi"
          active-text="EN"
          inactive-text="VI"
          @change="setLocale"
        />
      </div>
    </div>
  </header>
</template>

<style scoped>
/* HEADER WRAPPER */
.header-wrapper {
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.5);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.03);
}

/* CONTAINER */
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px; /* space trái phải */
  gap: 16px; /* khoảng cách giữa logo, menu, toggle */
}

/* LOGO */
.logo-text {
  font-size: 1.6rem;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #0004ff 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* MENU */
.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0; /* ⭐ quan trọng để flex-shrink hoạt động */
}

.custom-menu {
  width: 100%;
  border-bottom: none !important;
  background: transparent !important;
  height: 48px;
  padding: 4px 0;
  border-radius: 9999px;
  background-color: rgba(243, 244, 246, 0.55) !important;
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
}

:deep(.el-menu-item) {
  height: 40px !important;
  line-height: 40px !important;
  border-radius: 9999px;
  margin: 0 6px;
  padding: 0 24px !important;
  font-weight: 500;
  color: #64748b;
  white-space: nowrap !important;   /* không xuống dòng */
  overflow: visible !important;     /* không ẩn text */
  text-overflow: unset !important;  /* không 3 chấm */
  transition: all 0.2s ease;
}

:deep(.el-menu-item:hover) {
  color: #3b82f6 !important;
  background-color: rgba(255,255,255,0.85) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #ffffff !important;
  color: #3b82f6 !important;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

/* LANGUAGE TOGGLE */
.language-toggle {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 120px;
}

/* SWITCH */
:deep(.lang-switch .el-switch__core) {
  border-color: #e2e8f0;
  background-color: #f1f5f9;
}

:deep(.lang-switch.is-checked .el-switch__core) {
  border-color: #00ccff;
  background-color: #63d7f1;
}

:deep(.el-switch__label) {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
}

:deep(.el-switch__label.is-active) {
  color: #6366f1;
}

/* MOBILE RESPONSIVE */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    gap: 8px;
  }

  .nav-section {
    display: none; /* ẩn menu khi mobile */
  }

  .language-toggle {
    width: auto;
  }
}
</style>