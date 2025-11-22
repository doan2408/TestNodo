<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-content">
        <h1>{{ t("course.title") }}</h1>
        <p class="subtitle">{{ t("course.subtitle") }}</p>
      </div>
    </div>

    <div class="main-card">
      <!-- SEARCH AREA -->
      <div class="search-wrapper">
        <div class="search-inputs">
          <el-input
            v-model="keyword"
            :placeholder="t('course.searchPlaceholder')"
            clearable
            class="custom-input search-input"
            @input="searchCourses"
          >
            <template #prefix>
              <span class="search-icon">🔍</span>
            </template>
          </el-input>
        </div>
      </div>

      <!-- TOOLBAR: Thêm mới + Xuất Excel -->
      <div class="action-toolbar">
        <el-button type="primary" @click="createCourse" class="action-button">
          <span class="button-icon">➕</span>
          {{ t("course.addNew") }}
        </el-button>
        <el-button
          type="success"
          @click="handleExport"
          :loading="exporting"
          class="action-button"
        >
          <span class="button-icon" v-if="!exporting">📥</span>
          {{ t("course.exportExcel") }}
        </el-button>
      </div>

      <div class="list-container">
        <CourseTable
          ref="courseListRef"
          :keyword="keywordToSearch"
          :page="page"
          :size="size"
          :data="courses"
          :columns="columns"
          :totalElements="totalElements"
          :loadDataFn="loadCourses"
          :deleteFn="deleteCourseData"
          @pageChange="onPageChange"
          @edit="editCourse"
          @viewLessons="viewLessons"
          @viewStudents="viewStudents"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="closeDialog"
      destroy-on-close
      center
    >
      <CourseForm
        :course="selectedCourse"
        :isEdit="isEdit"
        :errors="formErrors"
        @save="saveCourse"
        @cancel="closeDialog"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";
import { useRouter } from "vue-router";
import CourseTable from "@/components/coursesComponent/CourseList.vue";
import CourseForm from "@/components/coursesComponent/CourseForm.vue";
import { ElMessage } from "element-plus";
import {
  getAllCourses,
  createCourse as apiCreateCourse,
  updateCourse as apiUpdateCourse,
  deleteCourse as apiDeleteCourse,
  exportCourses,
} from "@/api/CourseService";

import { useI18n } from "vue-i18n";

const { t } = useI18n();

const router = useRouter();

const dialogVisible = ref(false);
const selectedCourse = ref(null);
const isEdit = ref(false);
const dialogTitle = ref("");
const formErrors = ref([]);

// SEARCH
const keyword = ref("");
const keywordToSearch = ref("");

let searchTimeout = null;

// Pagination state
const page = ref(0);
const size = ref(10);

// Data state
const courses = ref([]);
const totalElements = ref(0);

// Export state
const exporting = ref(false);

// ref tới component list
const courseListRef = ref(null);

// Columns configuration
const columns = ref([
  { prop: "name", label: "Tên" },
  { prop: "code", label: "Mã" },
  { prop: "description", label: "Mô tả" },
  { prop: "status", label: "Trạng thái", width: "150" },
]);

// Load courses function
const loadCourses = async () => {
  try {
    const res = await getAllCourses({
      keyword: keywordToSearch.value || "",
      page: page.value,
      size: size.value,
    });
    courses.value = res.content || [];
    totalElements.value = res.totalElements || 0;
  } catch (err) {
    console.error(err);
    courses.value = [];
    totalElements.value = 0;
    ElMessage.error("Không thể tải danh sách khóa học"); // hoặc tạo key i18n nếu muốn
  }
};

// Delete function
const deleteCourseData = async (id) => {
  return await apiDeleteCourse(id);
};

// Export Excel
const handleExport = async () => {
  exporting.value = true;
  try {
    const blob = await exportCourses();
    const url = window.URL.createObjectURL(new Blob([blob]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "courses.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    ElMessage.success("Đã tải file Excel."); // hoặc t('common.success')
  } catch (err) {
    console.error(err);
    ElMessage.error("Xuất Excel thất bại.");
  } finally {
    exporting.value = false;
  }
};

const searchCourses = () => {
  if (searchTimeout) clearTimeout(searchTimeout);

  searchTimeout = setTimeout(async () => {
    page.value = 0;
    keywordToSearch.value = keyword.value;

    await nextTick();
    courseListRef.value?.loadCourses?.();
  }, 500);
};

// nhận event pageChange từ child pagination
const onPageChange = (newPage) => {
  page.value = newPage;
  courseListRef.value?.loadCourses?.();
};

// Tạo khóa học
const createCourse = () => {
  formErrors.value = [];
  selectedCourse.value = null;
  isEdit.value = false;
  dialogTitle.value = t("course.addNew");
  dialogVisible.value = true;
};

// Sửa khóa học
const editCourse = (course) => {
  formErrors.value = [];
  selectedCourse.value = { ...course };
  isEdit.value = true;
  dialogTitle.value = t("course.update");
  dialogVisible.value = true;
};

// Xem bài học của khóa học
const viewLessons = (courseId) => {
  router.push(`/courses/${courseId}/lessons`);
};

const viewStudents = (courseId) => {
  router.push(`courses/${courseId}/students`);
}

// Lưu dữ liệu
const saveCourse = async ({ course, thumbnailFile, deleteThumbnailIds }) => {
  formErrors.value = [];
  try {
    const formData = new FormData();
    formData.append("name", course.name);
    formData.append("code", course.code || "");
    formData.append("description", course.description || "");
    formData.append("status", course.status || "ACTIVE");

    if (thumbnailFile) {
      formData.append("thumbnail", thumbnailFile);
    }

    // nếu cần xóa ảnh cũ, append id(s)
    if (deleteThumbnailIds) {
      if (Array.isArray(deleteThumbnailIds)) {
        deleteThumbnailIds.forEach((id, index) => {
          formData.append(`deleteThumbnailIds[${index}]`, id);
        });
      } else {
        formData.append("deleteThumbnailIds", deleteThumbnailIds);
      }
    }

    if (course.id) {
      await apiUpdateCourse(course.id, formData);
      ElMessage.success(t("course.updateSuccess"));
    } else {
      await apiCreateCourse(formData);
      ElMessage.success(t("course.createSuccess"));
    }

    dialogVisible.value = false;
    courseListRef.value?.loadCourses?.();
  } catch (error) {
    console.error(error);
    // Xử lý lỗi từ backend
    const errorMessages = error.response?.data?.message;
    if (errorMessages) {
      if (Array.isArray(errorMessages)) {
        formErrors.value = errorMessages;
        const errorText = errorMessages.join("\n");
        ElMessage.error(errorText);
      } else if (typeof errorMessages === "string") {
        ElMessage.error(errorMessages);
      } else {
        ElMessage.error(t("common.saveFailed"));
      }
    } else {
      ElMessage.error(t("common.saveFailed"));
    }
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  formErrors.value = [];
};
</script>

<style scoped>
/* Variables */
:root {
  --primary-color: #4f46e5;
  --primary-hover: #4338ca;
  --bg-color: #f9fafb;
  --card-bg: #ffffff;
  --text-main: #111827;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
}

.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding: 40px 20px;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
  color: #1f2937;
}

/* Header Styles */
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
  letter-spacing: -0.025em;
}

.subtitle {
  font-size: 1.1rem;
  color: #6b7280;
  margin: 0;
}

/* Main Card Styles */
.main-card {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 32px;
  border: 1px solid #e5e7eb;
}

/* Search Area Styles */
.search-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 24px;
  background-color: #f9fafb;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  flex-wrap: wrap;
}

.search-inputs {
  display: flex;
  gap: 12px;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  max-width: 400px;
}

/* Action Toolbar Styles */
.action-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 0 20px;
}

.action-button {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.button-icon {
  font-size: 16px;
}

/* Element Plus Overrides (Deep Selectors) */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d1d5db inset;
  padding: 8px 12px;
  border-radius: 8px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #9ca3af inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #4f46e5 inset !important;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #4f46e5;
  --el-button-border-color: #4f46e5;
  --el-button-hover-bg-color: #4338ca;
  --el-button-hover-border-color: #4338ca;
}

/* Dialog Customization */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #111827;
  font-size: 1.125rem;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
