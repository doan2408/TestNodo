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
import { ElMessage, ElMessageBox } from "element-plus";
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
.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding: 40px 20px;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.header-section {
  max-width: 1200px;
  margin: 0 auto 32px;
  text-align: center;
}

.header-content h1 {
  font-size: 2.25rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 1.1rem;
  color: #6b7280;
  margin: 0;
}

.main-card {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  padding: 32px;
  border: 1px solid #e5e7eb;
}

.list-container {
  width: 100%;
}
</style>