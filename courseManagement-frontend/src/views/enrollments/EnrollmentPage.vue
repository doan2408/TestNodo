<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-content">
        <h1>Quản lý Sinh viên</h1>
        <p class="subtitle">Hệ thống quản lý hồ sơ và thông tin sinh viên</p>
      </div>
    </div>

    <div class="main-card">
      <!-- SEARCH AREA -->
      <div class="search-wrapper">
        <div class="search-inputs">
          <el-input
            v-model="keyword"
            placeholder="Tìm kiếm theo tên, email, sđt..."
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
            placeholder="Tất cả giới tính"
            clearable
            class="custom-select gender-select"
            @change="searchStudents"
          >
            <el-option label="Nam" value="1" />
            <el-option label="Nữ" value="0" />
          </el-select>

          <el-button type="danger" @click="showDeleteEnrollmentModal">
            Xóa đăng ký
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

    <!-- Dialog: Quản lý đăng ký khóa học -->
    <el-dialog
      v-model="enrollmentDialogVisible"
      title="Quản lý Đăng ký Khóa học"
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

    <!-- Modal: Xóa đăng ký -->
    <EnrollmentDeleteModal
      v-model="deleteEnrollmentVisible"
      :studentSelect="studentSelect"
      :courses="allCourses"
      @confirm="confirmDeleteEnrollment"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from "vue";
import StudentTable from "@/components/studentsComponent/StudentTable.vue";
import EnrollmentForm from "@/components/enrollmentComponent/EnrollmentForm.vue";
import EnrollmentDeleteModal from "@/components/enrollmentComponent/ModalDelete.vue";
import { ElMessage } from "element-plus";
import {
  getAllStudent,
  createStudent as apiCreateStudent,
  updateStudent as apiUpdateStudent,
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

const dialogVisible = ref(false);
const selectedStudent = ref(null);
const isEdit = ref(false);
const dialogTitle = ref("Tạo Sinh viên Mới");
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

// Pagination state
const page = ref(0);
const size = ref(10);

// Data state
const students = ref([]);
const totalElements = ref(0);

const studentListRef = ref(null);

const studentSelect = ref([]);

// Columns configuration
const columns = ref([
  { prop: "name", label: "Tên" },
  { prop: "email", label: "Email" },
  { prop: "phone", label: "Phone" },
  { prop: "gender", label: "Giới tính", width: "100" },
  { prop: "status", label: "Trạng thái", width: "120" },
]);

// Load courses
const loadCourses = async () => {
  try {
    const response = await getSelectCourses();
    allCourses.value = response.data || response || [];
  } catch (error) {
    console.error("Lỗi khi tải danh sách khóa học:", error);
    ElMessage.error("Không thể tải danh sách khóa học");
    allCourses.value = [];
  }
};

onMounted(() => {
  loadCourses();
  loadSelectStudents();
});

const loadSelectStudents = async () => {
  try {
    const response = await getSelectStudents();
    studentSelect.value = response;
  } catch (err) {
    throw err;
  }
};

console.log("student select: ", studentSelect.value);

// Load students function
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

// Export function
const exportStudentsData = async (params) => {
  return await exportStudents(params);
};

// Delete function
const deleteStudentData = async (id) => {
  return await apiDeleteStudent(id);
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
  dialogTitle.value = "Thêm Sinh viên Mới";
  dialogVisible.value = true;
};

const viewStudentEnrollment = async(student) => {
  dialogTitle.value = "Xem Thông tin";
  selectedStudent.value = { ...student };
  enrollmentLoading.value = true;
  isRead.value = true;
  enrollmentDialogVisible.value = true;

  try {
    const response = await getEnrollmentsByStudent(student.id);
    enrolledCourses.value = response.data || response || [];
    console.log("isRead: ", isRead.value)
    console.log("Enrolled courses:", enrolledCourses.value);
  } catch (error) {
    console.error("Lỗi khi tải danh sách đăng ký:", error);
    ElMessage.error("Không thể tải danh sách đăng ký");
    enrolledCourses.value = [];
  } finally {
    enrollmentLoading.value = false;
  }
};

// Xem đăng ký khóa học của sinh viên
const editStudentEnrollment = async (student) => {
  dialogTitle.value = "Xem Thông tin";
  selectedStudent.value = { ...student };
  enrollmentLoading.value = true;
  enrollmentDialogVisible.value = true;

  try {
    const response = await getEnrollmentsByStudent(student.id);
    enrolledCourses.value = response.data || response || [];
    console.log("Enrolled courses:", enrolledCourses.value);
  } catch (error) {
    console.error("Lỗi khi tải danh sách đăng ký:", error);
    ElMessage.error("Không thể tải danh sách đăng ký");
    enrolledCourses.value = [];
  } finally {
    enrollmentLoading.value = false;
  }
};

// Lưu đăng ký
const saveEnrollment = async (courseIds) => {
  if (!selectedStudent.value) return;

  enrollmentLoading.value = true;
  try {
    const enrollmentRequest = {
      studentId: selectedStudent.value.id,
      courseIds: courseIds,
    };

    await updateEnrollment(enrollmentRequest);
    ElMessage.success("Cập nhật đăng ký thành công!");
    enrollmentDialogVisible.value = false;
  } catch (error) {
    console.error("Lỗi khi cập nhật đăng ký:", error);
    const errorMessage =
      error.response?.data?.message || "Cập nhật đăng ký thất bại!";
    ElMessage.error(errorMessage);
  } finally {
    enrollmentLoading.value = false;
  }
};

// Đóng dialog enrollment
const closeEnrollmentDialog = () => {
  enrollmentDialogVisible.value = false;
  selectedStudent.value = null;
  enrolledCourses.value = [];
};

// Hiện modal xóa đăng ký
const showDeleteEnrollmentModal = () => {
  deleteEnrollmentVisible.value = true;
};

// Xác nhận xóa đăng ký
const confirmDeleteEnrollment = async ({ studentId, courseId }) => {
  try {
    await deleteEnrollment(studentId, courseId);
    ElMessage.success("Xóa đăng ký thành công!");
    deleteEnrollmentVisible.value = false;
  } catch (error) {
    console.error("Lỗi khi xóa đăng ký:", error);
    const errorMessage =
      error.response?.data?.message || "Xóa đăng ký thất bại!";
    ElMessage.error(errorMessage);
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  isRead.value = false;
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
