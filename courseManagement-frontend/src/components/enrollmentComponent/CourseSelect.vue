<template>
  <div>
    <el-select
      v-model="selectedCourses"
      multiple
      placeholder="Chọn khóa học"
      style="width: 100%"
    >
      <el-option
        v-for="course in courses"
        :key="course.id"
        :label="course.name"
        :value="course.id"
      />
    </el-select>

    <el-button type="primary" @click="assignCourses">Đăng ký khóa học</el-button>
  </div>
</template>

<script setup>
import { ref } from "vue";

const props = defineProps({
  courses: Array,
});

const emit = defineEmits(["select-courses"]);

const selectedCourses = ref([]);

const assignCourses = () => {
  emit("select-courses", selectedCourses.value);
};
</script>

<style scoped>

</style>
