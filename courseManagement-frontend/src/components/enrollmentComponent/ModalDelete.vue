<template>
  <el-dialog
    v-model="visible"
    title="Xóa Đăng Ký Khóa Học"
    width="500px"
    @close="handleClose"
  >
    <el-form label-width="120px">
      <!-- Sinh viên -->
      <el-form-item label="Sinh viên">
        <el-select
          v-model="selectedStudent"
          placeholder="Chọn sinh viên"
          style="width: 100%"
          filterable
          clearable
          @change="onStudentChange"
        >
          <el-option
            v-for="student in studentSelect"
            :key="student.id"
            :label="student.name"
            :value="student.id"
          >
            <span style="float: left">{{ student.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ student.email }} - {{ student.id }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- Khóa học -->
      <el-form-item label="Khóa học">
        <el-select
          v-model="selectedCourse"
          placeholder="Chọn khóa học cần xóa"
          style="width: 100%"
          filterable
          :loading="loadingCourses"
          :disabled="!selectedStudent"
        >
          <el-option
            v-for="course in studentCourses"
            :key="course.id"
            :label="`${course.name} - ${course.code}`"
            :value="course.id"
          >

          </el-option>
        </el-select>
        <div 
          v-if="selectedStudent && studentCourses.length === 0 && !loadingCourses" 
          style="margin-top: 8px; color: #909399; font-size: 12px"
        >
          ⚠️ Sinh viên này chưa đăng ký khóa học nào
        </div>
        <div 
          v-if="selectedStudent && studentCourses.length > 0" 
          style="margin-top: 8px; color: #67c23a; font-size: 12px"
        >
          ✓ Tìm thấy {{ studentCourses.length }} khóa học đã đăng ký
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">Hủy</el-button>
        <el-button
          type="danger"
          @click="handleConfirm"
          :disabled="!selectedStudent || !selectedCourse"
        >
          Xóa đăng ký
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { getEnrollmentsByStudent } from "@/api/EnrollmentService";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  studentSelect: { type: Array, default: () => [] },
  courses: { type: Array, default: () => [] },
});

const emit = defineEmits(["update:modelValue", "confirm"]);

const visible = ref(false);
const selectedStudent = ref(null);
const selectedCourse = ref(null);
const studentCourses = ref([]);
const loadingCourses = ref(false);

watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal;
    if (!newVal) {
      // Reset khi đóng
      resetForm();
    }
  }
);

watch(visible, (newVal) => {
  emit("update:modelValue", newVal);
});

const resetForm = () => {
  selectedStudent.value = null;
  selectedCourse.value = null;
  studentCourses.value = [];
  loadingCourses.value = false;
};

// Khi chọn sinh viên, load danh sách khóa học của sinh viên đó
const onStudentChange = async (studentId) => {
  selectedCourse.value = null; // Reset course selection
  studentCourses.value = [];
  
  if (!studentId) {
    return;
  }

  loadingCourses.value = true;
  try {
    const response = await getEnrollmentsByStudent(studentId);
    const enrollmentData = response.data || response || [];
    
    console.log("Raw enrollment data:", enrollmentData);
    
    // Xử lý data trả về: lấy courses từ mảng enrollmentData
    if (Array.isArray(enrollmentData) && enrollmentData.length > 0) {
      // Lấy courses từ enrollment đầu tiên (vì có thể trả về array với 1 phần tử)
      const enrollment = enrollmentData[0];
      
      if (enrollment.courses && Array.isArray(enrollment.courses)) {
        // Map courses từ cấu trúc API sang cấu trúc component
        studentCourses.value = enrollment.courses.map(course => ({
          id: course.courseId,
          title: course.courseName,
          name: course.courseName,
          code: course.courseCode,
        }));
      }
    }

    console.log("Student courses loaded:", studentCourses.value);
    
    if (studentCourses.value.length === 0) {
      ElMessage.info("Sinh viên này chưa đăng ký khóa học nào");
    }
  } catch (error) {
    console.error("Lỗi khi tải khóa học của sinh viên:", error);
    ElMessage.error("Không thể tải danh sách khóa học của sinh viên");
    studentCourses.value = [];
  } finally {
    loadingCourses.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
};

const handleConfirm = () => {
  if (selectedStudent.value && selectedCourse.value) {
    emit("confirm", {
      studentId: selectedStudent.value,
      courseId: selectedCourse.value,
    });
    resetForm();
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>