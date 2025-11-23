<template>
  <div class="page-container">
    <!-- Header Section -->
    <div class="header-section">
      <div class="header-content">
        <h1>{{ t("lesson.title") }}</h1>
        <p class="subtitle">{{ t("lesson.subtitle") }}</p>
      </div>
    </div>

    <!-- Main Card for Content -->
    <div class="main-card">
      <!-- Lesson List Section -->
      <div class="list-container">
        <LessonList
          ref="lessonListRef"
          :course-id="courseId"
          :page="page"
          :size="size"
          :courses="courses"
          @create="createLesson"
          @edit="editLesson"
          @delete="deleteLesson"
          @pageChange="onPageChange"
        />
      </div>
    </div>

    <!-- Dialog for Lesson Form -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="closeDialog"
      destroy-on-close
      center
    >
      <LessonForm
        :lesson="selectedLesson"
        :course-id="courseId"
        :isEdit="isEdit"
        :errors="formErrors"
        :courses="courses"
        @save="saveLesson"
        @cancel="closeDialog"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import LessonList from "@/components/lessonsComponent/LessonList.vue";
import LessonForm from "@/components/lessonsComponent/LessonForm.vue";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import {
  createLesson as apiCreateLesson,
  updateLesson as apiUpdateLesson,
  deleteLesson as apiDeleteLesson,
} from "@/api/LessonService";
import { getSelectCourses } from "@/api/CourseService";

// Get courseId from route params (optional)
const route = useRoute();
const courseId = ref(route.params.courseId ? Number(route.params.courseId) : null);

// i18n setup
const { t } = useI18n();

const dialogVisible = ref(false);
const selectedLesson = ref(null);
const isEdit = ref(false);
const dialogTitle = ref(t("lesson.addNew"));

// Pagination state
const page = ref(1);
const size = ref(10);

// Error state
const formErrors = ref([]);

// Ref to Lesson List component
const lessonListRef = ref(null);

// Courses list
const courses = ref([]);

// Load danh sách khóa học
const loadCourses = async () => {
  try {
    const response = await getSelectCourses();
    console.log("course select: ", response.data)
    courses.value = response.data || response || [];
  } catch (error) {
    console.error("Lỗi khi tải danh sách khóa học:", error);
    ElMessage.error(t("common.error"));
    courses.value = [];
  }
};

onMounted(() => {
  loadCourses();
});

// Create new lesson
const createLesson = () => {
  selectedLesson.value = null;
  isEdit.value = false;
  dialogTitle.value = t("lesson.addNew");
  formErrors.value = [];
  dialogVisible.value = true;
};

// Edit lesson
const editLesson = (lesson) => {
  selectedLesson.value = { ...lesson };
  isEdit.value = true;
  dialogTitle.value = t("lesson.update");
  formErrors.value = [];
  dialogVisible.value = true;
};

// Save lesson (create or update)
const saveLesson = async ({ lesson, videoFile, thumbnailFile, deleteVideoIds, deleteThumbnailIds }) => {
  formErrors.value = [];

  const loading = ElLoading.service({
    fullscreen: true,
    text: t("common.saving") || "Đang lưu...",
  });

  try {
    const formData = new FormData();
    formData.append("courseId", lesson.courseId);
    formData.append("title", lesson.title);

    if (videoFile) {
      formData.append("videos", videoFile);
    }

    if (thumbnailFile) {
      formData.append("thumbnails", thumbnailFile);
    }

    if (deleteVideoIds?.length > 0) {
      deleteVideoIds.forEach((id, i) => formData.append(`deleteVideoIds[${i}]`, id));
    }

    if (deleteThumbnailIds?.length > 0) {
      deleteThumbnailIds.forEach((id, i) => formData.append(`deleteThumbnailIds[${i}]`, id));
    }

    if (lesson.id) {
      await apiUpdateLesson(lesson.id, formData);
      ElMessage.success(t("lesson.updateSuccess"));
    } else {
      await apiCreateLesson(formData);
      ElMessage.success(t("lesson.createSuccess"));
    }
    dialogVisible.value = false;
    lessonListRef.value?.loadLessons?.();
  } catch (error) {
    const errorMessages = error.response?.data?.message;
    if (Array.isArray(errorMessages)) {
      formErrors.value = errorMessages;
      ElMessage.error(errorMessages.join("\n"));
    } else {
      formErrors.value = [errorMessages || t("lesson.saveFailed")];
      ElMessage.error(errorMessages || t("lesson.saveFailed"));
    }
  }
  finally {
    loading.close();
  }
};

// Delete lesson
const deleteLesson = async (id) => {
  try {
    await ElMessageBox.confirm(t("lesson.deleteConfirm"), t("common.confirm"), { 
      confirmButtonText: t("common.yes"), 
      cancelButtonText: t("common.no"), 
      type: "warning" 
    });
    await apiDeleteLesson(id);
    ElMessage.success(t("lesson.deleteSuccess"));
    lessonListRef.value?.loadLessons?.();
  } catch (error) {
    if (error !== "cancel") ElMessage.error(t("lesson.deleteFailed"));
  }
};

// Handle page change (pagination)
const onPageChange = (newPage) => {
  page.value = newPage;
};

// Close dialog
const closeDialog = () => {
  dialogVisible.value = false;
  formErrors.value = [];
};
</script>

<style scoped>
/* Modern Variables */
:root {
  --primary-color: #4f46e5;
  --primary-hover: #4338ca;
  --bg-color: #f9fafb;
  --card-bg: #ffffff;
  --text-main: #111827;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
  --shadow-soft: 0 4px 8px rgba(0,0,0,0.06);
  --shadow-medium: 0 6px 16px rgba(0,0,0,0.12);
}

/* Page Container */
.page-container {
  min-height: 100vh;
  background: linear-gradient(to bottom right, #eef2ff, #f3f4f6 40%);
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeIn 0.4s ease;
}

/* Header Section */
.header-section {
  max-width: 1200px;
  margin: 0 auto 32px;
  text-align: center;
  animation: slideDown 0.4s ease;
}

.header-content h1 {
  font-size: 2.4rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.subtitle {
  font-size: 1.15rem;
  margin-top: 8px;
  color: var(--text-secondary);
}

/* Main Card */
.main-card {
  width: 100%;
  max-width: 1200px;
  background: var(--card-bg);
  border-radius: 18px;
  padding: 32px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-soft);
  transition: 0.3s ease;
  animation: fadeUp 0.5s ease;
}

.main-card:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-3px);
}

/* List Container */
.list-container {
  margin-top: 10px;
}

/* El-Dialog Styling */
:deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: var(--shadow-medium);
  animation: scaleIn 0.3s ease;
}

:deep(.el-dialog__header) {
  padding: 18px 24px;
  background-color: #f9fafb;
  border-bottom: 1px solid var(--border-color);
}

:deep(.el-dialog__title) {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-main);
}

:deep(.el-dialog__body) {
  padding: 24px;
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-15px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes scaleIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>