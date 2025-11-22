<template>
  <div class="course-list-container">
    <!-- Table -->
    <div class="table-wrapper">
      <el-table :data="data" style="width: 100%" :max-height="tableMaxHeight">
        <el-table-column label="STT" width="60">
          <template #default="scope">
            {{ page * size + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- Thumbnail column (nếu data có thumbnail) -->
        <el-table-column label="Thumbnail" width="80" v-if="hasThumbnail">
          <template #default="scope">
            <el-image
              v-if="scope.row.thumbnail?.[0]?.url"
              :src="scope.row.thumbnail[0].url"
              style="
                width: 50px;
                height: 50px;
                border-radius: 4px;
                object-fit: cover;
              "
            />
            <span v-else style="color: #999; font-size: 12px">No image</span>
          </template>
        </el-table-column>

        <!-- Render các cột động -->
        <el-table-column
          v-for="col in columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
        >
          <!-- Custom render cho status -->
          <template #default="scope" v-if="col.prop === 'status'">
            <el-tag type="success" v-if="scope.row.status == '1'"
              >Hoạt động</el-tag
            >
            <el-tag type="danger" v-else>Tắt</el-tag>
          </template>
        </el-table-column>

        <!-- Thao tác: Sửa / Xem bài / Xóa -->
        <el-table-column label="Thao tác" width="340">
          <template #default="scope">
            <el-button size="small" @click="$emit('edit', scope.row)"
              >Sửa</el-button
            >
            <el-button size="small" @click="$emit('viewStudents', scope.row.id)"
              >xem học sinh</el-button
            >
            <el-button
              size="small"
              type="warning"
              @click="$emit('viewLessons', scope.row.id)"
            >
              Xem bài
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="confirmDeleteCourse(scope.row.id)"
            >
              Xóa
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalElements"
        :page-size="size"
        :current-page="page + 1"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

// props
const props = defineProps({
  keyword: { type: String, default: "" },
  page: { type: Number, default: 0 },
  size: { type: Number, default: 10 },
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  totalElements: { type: Number, default: 0 },
  loadDataFn: { type: Function, required: true },
  deleteFn: { type: Function, default: null },
});

const emit = defineEmits(["edit", "delete", "viewLessons", "pageChange"]);

// state
const tableMaxHeight = ref("60vh");

// Check nếu data có thumbnail
const hasThumbnail = computed(() => {
  return props.data.some((item) => item.thumbnail);
});

// responsive table height
const updateTableHeight = () => {
  const headerEstimated = 180;
  const available = window.innerHeight - headerEstimated;
  tableMaxHeight.value = `${Math.max(300, available)}px`;
};

// load data function
const loadCourses = async () => {
  try {
    await props.loadDataFn();
  } catch (err) {
    console.error(err);
  }
};

// watch props changes
watch(
  () => [props.keyword, props.page, props.size],
  () => {
    loadCourses();
  },
  { immediate: true }
);

// expose
defineExpose({ loadCourses });

// pagination
const changePage = (p) => emit("pageChange", p - 1);

// Xóa khóa học với confirm Box
const confirmDeleteCourse = async (id) => {
  if (!props.deleteFn) {
    ElMessage.warning("Chức năng xóa chưa được cấu hình.");
    return;
  }

  try {
    await ElMessageBox.confirm(
      "Bạn có chắc muốn xóa khóa học này không?",
      "Xác nhận",
      {
        confirmButtonText: "Có",
        cancelButtonText: "Hủy",
        type: "warning",
      }
    );

    // Nếu xác nhận, gọi deleteFn
    await props.deleteFn(id);
    ElMessage.success("Xóa khóa học thành công!");
    loadCourses();
  } catch (error) {
    // Nếu nhấn Hủy thì không báo lỗi
    if (error !== "cancel") {
      ElMessage.error("Xóa thất bại!");
      console.error(error);
    }
  }
};

onMounted(() => {
  updateTableHeight();
  window.addEventListener("resize", updateTableHeight);
});
</script>

<style scoped>
.course-list-container {
  padding: 20px;
}

.table-wrapper {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}
</style>
