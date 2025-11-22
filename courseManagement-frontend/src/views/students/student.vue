<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-content">
        <h1>Quản lý Sinh viên</h1>
        <p class="subtitle">Hệ thống quản lý hồ sơ và thông tin sinh viên</p>
      </div>
    </div>

    <div class="main-card">
      SEARCH AREA
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
        </div>

        <!-- <el-button type="primary" @click="onSearch" class="search-button">
          Tìm kiếm
        </el-button> -->
        <!-- <el-button type="primary" @click="refresh" class="search-button">
          Refresh
        </el-button> -->
      </div>

      <div class="list-container">
        <StudentList
          ref="studentListRef"
          :keyword="keywordToSearch"
          :gender="genderToSearch"
          :page="page"
          :size="size"
          @pageChange="onPageChange"
          @edit="editStudent"
          @delete="deleteStudent"
          @create="createStudent"
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
        :errors="formErrors"
        @save="saveStudent"
        @cancel="closeDialog"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";
import StudentList from "@/components/studentsComponent/StudentList.vue";
import StudentForm from "@/components/studentsComponent/StudentForm.vue";
import { ElMessage } from "element-plus";
import {
  createStudent as apiCreateStudent,
  updateStudent as apiUpdateStudent,
  deleteStudent as apiDeleteStudent,
} from "@/api/StudentService";

const dialogVisible = ref(false);
const selectedStudent = ref(null);
const isEdit = ref(false);
const dialogTitle = ref("Tạo Sinh viên Mới");

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

// ref tới component list (để reload list)
const studentListRef = ref(null);

const searchStudents = () => {
  if (searchTimeout) clearTimeout(searchTimeout);

  searchTimeout = setTimeout(async () => {
    page.value = 0;
    keywordToSearch.value = keyword.value;
    genderToSearch.value = gender.value;

    await nextTick(); // đợi prop cập nhật xuống child
    studentListRef.value?.loadStudents?.();
  }, 500);
};

// SEARCH: cập nhật prop và reload child
const onSearch = async () => {
  // reset to first page on new search
  page.value = 0;
  keywordToSearch.value = keyword.value;
  genderToSearch.value = gender.value;

  // đợi Vue cập nhật prop xuống child trước khi gọi loadStudents
  await nextTick();
  studentListRef.value?.loadStudents?.();
};

const refresh = async () => {
  page.value = 0;
  keywordToSearch.value = "";
  genderToSearch.value = "";
};

// nhận event pageChange từ child pagination
const onPageChange = (newPage) => {
  page.value = newPage;
  // optional: child watch sẽ tự reload, no need to call loadStudents here
  studentListRef.value?.loadStudents?.();
};

// Tạo sinh viên
const createStudent = () => {
  selectedStudent.value = null;
  isEdit.value = false;
  dialogTitle.value = "Thêm Sinh viên Mới";
  dialogVisible.value = true;
};

// Sửa sinh viên
const editStudent = (student) => {
  formErrors.value = []; // reset lỗi
  selectedStudent.value = { ...student };
  isEdit.value = true;
  dialogTitle.value = "Cập nhật Thông tin";
  dialogVisible.value = true;
};

// Xem chi tiết
const viewStudentDetail = (student) => {
  formErrors.value = []; // reset lỗi
  selectedStudent.value = { ...student };
  isEdit.value = false;
  dialogTitle.value = "Hồ sơ Sinh viên";
  dialogVisible.value = true;
};

// Lưu dữ liệu
const saveStudent = async ({ student, avatarFile, deleteAvatarIds }) => {
  formErrors.value = []; // reset lỗi
  try {
    const formData = new FormData();
    formData.append("name", student.name);
    formData.append("email", student.email);
    formData.append("phone", student.phone);
    formData.append("gender", student.gender);

    if (avatarFile) {
      formData.append("avatar", avatarFile);
    }

    // append deleteAvatarIds (từng phần tử nếu array, hoặc JSON string)
    if (deleteAvatarIds && deleteAvatarIds.length > 0) {
      deleteAvatarIds.forEach((id, index) => {
        formData.append(`deleteAvatarIds[${index}]`, id);
      });
    }

    if (student.id) {
      await apiUpdateStudent(student.id, formData);
      ElMessage.success("Cập nhật thành công!");
    } else {
      await apiCreateStudent(formData);
      ElMessage.success("Tạo sinh viên thành công!");
    }

    dialogVisible.value = false;
    studentListRef.value?.loadStudents?.();
  } catch (error) {
    console.error(error);
    // Xử lý lỗi từ backend
    const errorMessages = error.response?.data?.message;
    if (errorMessages) {
      if (Array.isArray(errorMessages)) {
        formErrors.value = errorMessages; // lưu errors để hiển thị trong form
        const errorText = errorMessages.join("\n");
        ElMessage.error(errorText);
      } else if (typeof errorMessages === "string") {
        ElMessage.error(errorMessages);
      } else {
        ElMessage.error("Lưu thất bại!");
      }
    } else {
      ElMessage.error("Lưu thất bại!");
    }
  }
};

// Xóa sinh viên
const deleteStudent = async (id) => {
  try {
    await apiDeleteStudent(id);
    ElMessage.success("Xóa thành công!");
    studentListRef.value?.loadStudents?.();
  } catch (error) {
    ElMessage.error("Xóa thất bại!");
  }
};

const closeDialog = () => (dialogVisible.value = false);
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
  background-color: #f3f4f6; /* Light gray background */
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

.search-button {
  min-width: 120px;
  height: 40px;
  font-weight: 600;
  background-color: #4f46e5;
  border-color: #4f46e5;
  transition: all 0.2s ease;
}

.search-button:hover {
  background-color: #4338ca;
  border-color: #4338ca;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);
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
