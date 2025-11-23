<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-content">
        <h1>{{ t('student.title') }}</h1>
        <p class="subtitle">{{ t('student.subtitle') }}</p>
      </div>
    </div>

    <div class="main-card">
      <!-- SEARCH AREA -->
      <div class="search-wrapper">
        <div class="search-inputs">
          <el-input
            v-model="keyword"
            :placeholder="t('student.searchPlaceholder')"
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
            :placeholder="t('student.gender')"
            clearable
            class="custom-select gender-select"
            @change="searchStudents"
          >
            <el-option :label="t('student.male')" value="1" />
            <el-option :label="t('student.female')" value="0" />
          </el-select>

          <el-button type="danger" @click="showDeleteEnrollmentModal">
            {{ t('enrollment.delete') }}
          </el-button>
        </div>
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
          :exportFn="exportStudentsData"
          :deleteFn="deleteStudentData"
          @pageChange="onPageChange"
          @edit="editStudentEnrollment"
          @create="createStudent"
          @detail="viewStudentEnrollment"
        />
      </div>
    </div>

    <!-- Dialog: Enrollment Management -->
    <el-dialog
      v-model="enrollmentDialogVisible"
      :title="t('enrollment.title')"
      width="600px"
      @close="closeEnrollmentDialog"
      destroy-on-close
    >
      <EnrollmentForm
        v-if="selectedStudent"
        :disabled="isRead"
        :student="selectedStudent"
        :enrolled-courses="enrolledCourses"
        :all-courses="allCourses"
        :loading="enrollmentLoading"
        @save="saveEnrollment"
        @cancel="closeEnrollmentDialog"
      />
    </el-dialog>

    <!-- Modal: Delete Enrollment -->
    <EnrollmentDeleteModal
      v-model="deleteEnrollmentVisible"
      :studentSelect="studentSelect"
      :courses="allCourses"
      @confirm="confirmDeleteEnrollment"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import StudentTable from "@/components/studentsComponent/StudentTable.vue";
import EnrollmentForm from "@/components/enrollmentComponent/EnrollmentForm.vue";
import EnrollmentDeleteModal from "@/components/enrollmentComponent/ModalDelete.vue";
import { ElLoading, ElMessage } from "element-plus";
import {
  getAllStudent,
  deleteStudent as apiDeleteStudent,
  exportStudents,
  getSelectStudents,
} from "@/api/StudentService";
import { getSelectCourses } from "@/api/CourseService";
import {
  getEnrollmentsByStudent,
  updateEnrollment,
  deleteEnrollment,
} from "@/api/EnrollmentService";

import { useI18n } from "vue-i18n";

const { t } = useI18n();

// STATE
const dialogVisible = ref(false);
const selectedStudent = ref(null);
const isEdit = ref(false);
const dialogTitle = ref("");
const formErrors = ref([]);
const isRead = ref(false);

// Enrollment state
const enrollmentDialogVisible = ref(false);
const deleteEnrollmentVisible = ref(false);
const enrolledCourses = ref([]);
const allCourses = ref([]);
const enrollmentLoading = ref(false);

// SEARCH
const keyword = ref("");
const gender = ref("");
const keywordToSearch = ref("");
const genderToSearch = ref("");

let searchTimeout = null;

// Pagination
const page = ref(0);
const size = ref(10);

// Data
const students = ref([]);
const totalElements = ref(0);
const studentListRef = ref(null);
const studentSelect = ref([]);

// Columns configuration
const columns = ref([
  { prop: "name", label: "Name" },
  { prop: "email", label: "Email", width: "180" },
  { prop: "phone", label: "Phone" },
  { prop: "gender", label: "Gender", width: "100" },
  { prop: "status", label: "Status", width: "120" },
]);

// LOAD COURSES
const loadCourses = async () => {
  try {
    const response = await getSelectCourses();
    allCourses.value = response.data || response || [];
  } catch (error) {
    console.error(t('enrollment.loadCoursesFailed'), error);
    ElMessage.error(t('enrollment.loadCoursesFailed'));
    allCourses.value = [];
  }
};

// LOAD STUDENTS
const loadSelectStudents = async () => {
  try {
    const response = await getSelectStudents();
    studentSelect.value = response;
  } catch (err) {
    throw err;
  }
};

// Load students data
const loadStudents = async () => {
  try {
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
  } catch (err) {
    console.error(err);
    students.value = [];
    totalElements.value = 0;
  }
};

// Export
const exportStudentsData = async (params) => await exportStudents(params);

// Delete
const deleteStudentData = async (id) => await apiDeleteStudent(id);

// SEARCH
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

// Pagination
const onPageChange = (newPage) => {
  page.value = newPage;
  studentListRef.value?.loadStudents?.();
};

// CREATE / VIEW / EDIT
const createStudent = () => {
  formErrors.value = [];
  selectedStudent.value = null;
  isEdit.value = false;
  dialogTitle.value = t('student.addNew');
  dialogVisible.value = true;
};

const viewStudentEnrollment = async(student) => {
  dialogTitle.value = t('student.profile');
  selectedStudent.value = { ...student };
  enrollmentLoading.value = true;
  isRead.value = true;
  enrollmentDialogVisible.value = true;

  try {
    const response = await getEnrollmentsByStudent(student.id);
    enrolledCourses.value = response.data || response || [];
  } catch (error) {
    console.error(t('enrollment.loadFailed'), error);
    ElMessage.error(t('enrollment.loadFailed'));
    enrolledCourses.value = [];
  } finally {
    enrollmentLoading.value = false;
  }
};

// EDIT enrollment
const editStudentEnrollment = async (student) => {
  dialogTitle.value = t('student.profile');
  selectedStudent.value = { ...student };
  enrollmentLoading.value = true;
  enrollmentDialogVisible.value = true;

  try {
    const response = await getEnrollmentsByStudent(student.id);
    enrolledCourses.value = response.data || response || [];
  } catch (error) {
    console.error(t('enrollment.loadFailed'), error);
    ElMessage.error(t('enrollment.loadFailed'));
    enrolledCourses.value = [];
  } finally {
    enrollmentLoading.value = false;
  }
};

// SAVE enrollment
const saveEnrollment = async (courseIds) => {
  if (!selectedStudent.value) return;

  const loading = ElLoading.service({
    fullscreen: true,
    text: t("common.saving") || "Đang lưu...",
  });
  try {
    await updateEnrollment({
      studentId: selectedStudent.value.id,
      courseIds,
    });
    ElMessage.success(t('enrollment.saveSuccess'));
    enrollmentDialogVisible.value = false;
  } catch (error) {
    console.error(t('enrollment.saveFailed'), error);
    const errorMessage =
      error.response?.data?.message || t('enrollment.saveFailed');
    ElMessage.error(errorMessage);
  } finally {
    loading.close();
  }
};

// CLOSE enrollment dialog
const closeEnrollmentDialog = () => {
  enrollmentDialogVisible.value = false;
  selectedStudent.value = null;
  enrolledCourses.value = [];
};

// DELETE enrollment
const showDeleteEnrollmentModal = () => {
  deleteEnrollmentVisible.value = true;
};

const confirmDeleteEnrollment = async ({ studentId, courseId }) => {
  try {
    await deleteEnrollment(studentId, courseId);
    ElMessage.success(t('enrollment.deleteSuccess'));
    deleteEnrollmentVisible.value = false;
  } catch (error) {
    console.error(t('enrollment.deleteFailed'), error);
    const errorMessage =
      error.response?.data?.message || t('enrollment.deleteFailed');
    ElMessage.error(errorMessage);
  }
};

// INITIAL LOAD
onMounted(() => {
  loadCourses();
  loadSelectStudents();
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

.search-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
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
