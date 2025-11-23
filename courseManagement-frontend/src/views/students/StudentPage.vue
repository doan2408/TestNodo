<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-content">
        <h1>{{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
    </div>

    <div class="main-card">
      <!-- SEARCH AREA - Ẩn khi xem theo khóa học -->
      <div class="search-wrapper" v-if="!courseId">
        <div class="search-inputs">
          <el-input
            v-model="keyword"
            :placeholder="$t('student.searchPlaceholder')"
            clearable
            class="custom-input search-input"
            @input="searchStudents"
          >
            <template #prefix>
              <span class="search-icon">🔍</span>
            </template>
          </el-input>

          <el-select
            v-model="gender"
            :placeholder="$t('common.none')"
            clearable
            class="custom-select gender-select"
            @change="searchStudents"
          >
            <el-option :label="$t('student.male')" value="1" />
            <el-option :label="$t('student.female')" value="0" />
          </el-select>
        </div>
      </div>

      <!-- TOOLBAR: Thêm mới + Xuất Excel - Ẩn khi xem theo khóa học -->
      <div class="action-toolbar" v-if="!courseId">
        <el-button type="primary" @click="createStudent" class="action-button">
          <span class="button-icon">➕</span>
          {{ $t("student.addNew") }}
        </el-button>
        <el-button
          type="success"
          @click="handleExport"
          :loading="exporting"
          class="action-button"
        >
          <span class="button-icon" v-if="!exporting">📥</span>
          {{ $t("student.exportExcel") }}
        </el-button>
      </div>

      <!-- Back button khi xem theo khóa học -->
      <div class="action-toolbar" v-else>
        <el-button @click="goBackToCourses" class="action-button">
          <span class="button-icon">⬅️</span>
          {{ $t("course.title") }}
        </el-button>
      </div>

      <div class="list-container">
        <StudentTable
          ref="studentListRef"
          :keyword="keywordToSearch"
          :gender="genderToSearch"
          :page="page"
          :size="size"
          :data="students"
          :columns="columns"
          :totalElements="totalElements"
          :loadDataFn="loadStudents"
          :deleteFn="courseId ? null : deleteStudentData"
          @pageChange="onPageChange"
          @edit="editStudent"
          @detail="viewStudentDetail"
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
      <StudentForm
        :student="selectedStudent"
        :isEdit="isEdit"
        :isRead="isRead"
        :disabled="isRead"
        :errors="formErrors"
        @save="saveStudent"
        @cancel="closeDialog"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import StudentTable from "@/components/studentsComponent/StudentTable.vue";
import StudentForm from "@/components/studentsComponent/StudentForm.vue";
import { ElMessage, ElLoading } from "element-plus";
import {
  getAllStudent,
  createStudent as apiCreateStudent,
  updateStudent as apiUpdateStudent,
  deleteStudent as apiDeleteStudent,
  exportStudents,
} from "@/api/StudentService";
import { getStudentsByCourse } from "@/api/EnrollmentService";

const { t } = useI18n();

const route = useRoute();
const router = useRouter();

// Lấy courseId từ route params
const courseId = computed(() => {
  const id = route.params.courseId;
  return id ? Number(id) : null;
});

// Dynamic page title
const pageTitle = computed(() => {
  return courseId.value
    ? t("student.title") + " - " + t("course.title")
    : t("student.title");
});

const pageSubtitle = computed(() => {
  return courseId.value ? t("student.subtitle") : t("student.subtitle");
});

const dialogVisible = ref(false);
const selectedStudent = ref(null);
const isEdit = ref(false);
const isRead = ref(false);
const dialogTitle = ref(t("student.addNew"));

const formErrors = ref([]); // state để lưu lỗi hiển thị trong form

// SEARCH
const keyword = ref("");
const gender = ref("");
const keywordToSearch = ref("");
const genderToSearch = ref("");

let searchTimeout = null;

// Pagination state (parent controls current page/size and passes to child)
const page = ref(0);
const size = ref(10);

// Data state
const students = ref([]);
const totalElements = ref(0);

// Export state
const exporting = ref(false);

// ref tới component list (để reload list)
const studentListRef = ref(null);

// Columns configuration
const columns = ref([
  { prop: "name", label: t("student.name") },
  { prop: "email", label: t("student.email"), width: "200" },
  { prop: "phone", label: t("student.phone") },
  { prop: "gender", label: t("student.gender"), width: "100" },
  { prop: "status", label: t("common.status") || "Status", width: "120" },
]);

// Load students function - phân biệt theo courseId
const loadStudents = async () => {
  try {
    if (courseId.value) {
      const res = await getStudentsByCourse(courseId.value);
      students.value = res || [];
      totalElements.value = res?.length || 0;
    } else {
      const res = await getAllStudent({
        keyword: keywordToSearch.value || "",
        gender: genderToSearch.value || "",
        page: page.value,
        size: size.value,
        sortBy: "id",
        sortDirection: "DESC",
      });
      students.value = res.content || [];
      totalElements.value = res.totalElements || 0;
    }
  } catch (err) {
    console.error(err);
    students.value = [];
    totalElements.value = 0;
    ElMessage.error(t("common.error"));
  }
};

// Delete function
const deleteStudentData = async (id) => {
  return await apiDeleteStudent(id);
};

// Export Excel
const handleExport = async () => {
  exporting.value = true;
  try {
    const blob = await exportStudents({
      keyword: keywordToSearch.value || "",
      gender: genderToSearch.value || "",
    });
    const url = window.URL.createObjectURL(new Blob([blob]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "students.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    ElMessage.success(t("common.success"));
  } catch (err) {
    console.error(err);
    ElMessage.error(t("common.error"));
  } finally {
    exporting.value = false;
  }
};

const searchStudents = () => {
  if (searchTimeout) clearTimeout(searchTimeout);

  searchTimeout = setTimeout(async () => {
    page.value = 0;
    keywordToSearch.value = keyword.value;
    genderToSearch.value = gender.value;

    await nextTick();
    studentListRef.value?.loadStudents?.();
  }, 500);
};

const onPageChange = (newPage) => {
  page.value = newPage;
  studentListRef.value?.loadStudents?.();
};

const createStudent = () => {
  formErrors.value = [];
  selectedStudent.value = null;
  isEdit.value = false;
  dialogTitle.value = t("student.addNew");
  dialogVisible.value = true;
};

const editStudent = (student) => {
  if (courseId.value) {
    ElMessage.warning(t("common.error"));
    return;
  }
  formErrors.value = [];
  selectedStudent.value = { ...student };
  isEdit.value = true;
  dialogTitle.value = t("student.update");
  dialogVisible.value = true;
};

const viewStudentDetail = (student) => {
  formErrors.value = [];
  selectedStudent.value = { ...student };
  isEdit.value = false;
  isRead.value = true;
  nextTick(() => {
    dialogTitle.value = t("student.profile");
    dialogVisible.value = true;
  });
};

const goBackToCourses = () => {
  router.push("/courses");
};

const saveStudent = async ({ student, avatarFile, deleteAvatarIds }) => {
  formErrors.value = [];
  const loading = ElLoading.service({
    fullscreen: true,
    text: t("common.saving") || "Đang lưu...",
  });

  try {
    const formData = new FormData();
    formData.append("name", student.name);
    formData.append("email", student.email);
    formData.append("phone", student.phone);
    formData.append("gender", student.gender);

    if (avatarFile) {
      formData.append("avatar", avatarFile);
    }

    if (deleteAvatarIds && deleteAvatarIds.length > 0) {
      deleteAvatarIds.forEach((id, index) => {
        formData.append(`deleteAvatarIds[${index}]`, id);
      });
    }

    if (student.id) {
      await apiUpdateStudent(student.id, formData);
      ElMessage.success(t("student.updateSuccess"));
    } else {
      await apiCreateStudent(formData);
      ElMessage.success(t("student.createSuccess"));
    }

    dialogVisible.value = false;
    studentListRef.value?.loadStudents?.();
  } catch (error) {
    console.error(error);
    const errorMessages = error.response?.data?.message;
    if (errorMessages) {
      if (Array.isArray(errorMessages)) {
        formErrors.value = errorMessages;
        const errorText = errorMessages.join("\n");
        ElMessage.error(errorText);
      } else if (typeof errorMessages === "string") {
        ElMessage.error(errorMessages);
      } else {
        ElMessage.error(t("student.saveFailed"));
      }
    } else {
      ElMessage.error(t("student.saveFailed"));
    }
  } finally {
    loading.close();
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  isRead.value = false;
};

onMounted(() => {
  loadStudents();
});
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

.gender-select {
  width: 180px;
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
