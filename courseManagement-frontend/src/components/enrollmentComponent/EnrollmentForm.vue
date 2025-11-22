<template>
  <div class="enrollment-form">
    <!-- Thông tin sinh viên -->
    <div class="student-info" v-if="student">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Tên sinh viên">
          {{ student.name }}
        </el-descriptions-item>
        <el-descriptions-item label="Email">
          {{ student.email }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- Chọn khóa học -->
    <el-form-item label="Khóa học đã đăng ký" style="margin-top: 20px">
      <el-select
        v-model="selectedCourses"
        multiple
        placeholder="Chọn khóa học"
        style="width: 100%"
        filterable
        :loading="loading"
      >
        <el-option
          v-for="course in allCourses"
          :key="course.id"
          :label="`${course.name} - ${course.code}`"
          :value="course.id"
        >
          <span style="float: left">{{ course.title || course.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">
            ID: {{ course.id }}
          </span>
        </el-option>
      </el-select>
      <div style="margin-top: 8px; font-size: 12px; color: #909399">
        💡 Đã chọn {{ selectedCourses.length }} khóa học
      </div>
    </el-form-item>

    <!-- Actions -->
    <div class="form-actions">
      <el-button type="primary" @click="handleSave" :loading="loading">
        Lưu thay đổi
      </el-button>
      <el-button @click="handleCancel">Hủy</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  student: { type: Object, required: true },
  enrolledCourses: { type: Array, default: () => [] },
  allCourses: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
});

import { useI18n } from "vue-i18n";

const { t } = useI18n();

const emit = defineEmits(["save", "cancel"]);

const selectedCourses = ref([]);

// Watch enrolledCourses để cập nhật selectedCourses
watch(
  () => props.enrolledCourses,
  (newVal) => {
    if (Array.isArray(newVal) && newVal.length > 0) {
      // Xử lý data từ API: enrolledCourses có thể là array với structure như bạn cung cấp
      const enrollment = newVal[0]; // Lấy phần tử đầu tiên
      
      if (enrollment.courses && Array.isArray(enrollment.courses)) {
        // Map courseId từ mảng courses
        selectedCourses.value = enrollment.courses.map(course => course.courseId);
      } else if (Array.isArray(newVal[0])) {
        // Hoặc nếu là array of courses trực tiếp
        selectedCourses.value = newVal.map(course => 
          course.courseId || course.id
        );
      }
    } else {
      selectedCourses.value = [];
    }
  },
  { immediate: true, deep: true }
);

const handleSave = () => {
  emit("save", selectedCourses.value);
};

const handleCancel = () => {
  emit("cancel");
};
</script>

<style scoped>
.enrollment-form {
  padding: 20px;
}

.student-info {
  margin-bottom: 24px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}
</style>