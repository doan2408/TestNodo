<template>
  <div class="student-list-container">
    <!-- Toolbar: tạo mới + xuất excel -->
    <div class="toolbar">
      <el-button type="primary" @click="$emit('create')">Tạo Sinh viên Mới</el-button>
      <el-button type="success" @click="handleExport" :loading="exporting">Xuất Excel</el-button>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <el-table :data="students" style="width: 100%" :max-height="tableMaxHeight">
        <el-table-column label="STT" width="60">
          <template #default="scope">
            {{ props.page * props.size + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Tên" />
        <el-table-column label="Avatar" width="80">
          <template #default="scope">
            <img
              v-if="scope.row.avatar?.length"
              :src="scope.row.avatar[0].url"
              alt="avatar"
              style="width: 45px; height: 45px; border-radius: 50%; object-fit: cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" />
        <el-table-column prop="phone" label="Phone" />
        <el-table-column label="Giới tính" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.gender === '1'">Nam</el-tag>
            <el-tag type="info" v-else>Nữ</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Trạng thái" width="120">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === '1'">Hoạt động</el-tag>
            <el-tag type="danger" v-else>Khóa</el-tag>
          </template>
        </el-table-column>
        <!-- Thao tác: Sửa / Xem / Xóa -->
        <el-table-column label="Thao tác" width="220">
          <template #default="scope">
            <el-button size="small" @click="$emit('edit', scope.row)">Sửa</el-button>

            <!-- Xóa với confirm Box -->
            <el-button size="small" type="danger" @click="confirmDeleteStudent(scope.row.id)">
              Xóa
            </el-button>

            <el-button size="small" @click="$emit('detail', scope.row)">Xem</el-button>
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
import { ref, onMounted, watch } from "vue";
import { getAllStudent, exportStudents, deleteStudent as deleteStudentApi } from "@/api/StudentService";
import { ElMessage, ElMessageBox } from "element-plus";

// props
const props = defineProps({
  keyword: { type: String, default: "" },
  gender: { type: String, default: "" },
  page: { type: Number, default: 0 },
  size: { type: Number, default: 10 }
});

const emit = defineEmits(["create", "edit", "delete", "detail", "pageChange"]);

// state
const students = ref([]);
const totalElements = ref(0);
const exporting = ref(false);
const tableMaxHeight = ref("60vh");

// responsive table height
const updateTableHeight = () => {
  const headerEstimated = 180;
  const available = window.innerHeight - headerEstimated;
  tableMaxHeight.value = `${Math.max(300, available)}px`;
};

// load data function
let searchTimeout = null;
const loadStudents = async () => {
  try {
    const res = await getAllStudent({
      keyword: props.keyword || "",
      gender: props.gender || "",
      page: props.page,
      size: props.size,
      sortBy: "id",
      sortDirection: "DESC"
    });
    students.value = res.content || [];
    totalElements.value = res.totalElements || 0;
  } catch (err) {
    console.error(err);
    students.value = [];
    totalElements.value = 0;
  }
};

watch(
  () => [props.keyword, props.gender, props.page, props.size],
  () => {
      loadStudents(); // load khi props thay đổi
  },
  { immediate: true }
);

// expose
defineExpose({ loadStudents });

// pagination
const changePage = (p) => emit("pageChange", p - 1);

// export Excel
const handleExport = async () => {
  exporting.value = true;
  try {
    const blob = await exportStudents({
      keyword: props.keyword || "",
      gender: props.gender || ""
    });
    const url = window.URL.createObjectURL(new Blob([blob]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "students.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    ElMessage.success("Đã tải file Excel.");
  } catch (err) {
    console.error(err);
    ElMessage.error("Xuất Excel thất bại.");
  } finally {
    exporting.value = false;
  }
};

// Xóa sinh viên với confirm Box
const confirmDeleteStudent = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc muốn xóa sinh viên này không?',
      'Xác nhận',
      {
        confirmButtonText: 'Có',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    );

    // Nếu xác nhận, gọi API xóa
    await deleteStudentApi(id);
    ElMessage.success('Xóa sinh viên thành công!');
    loadStudents();
  } catch (error) {
    // Nếu nhấn Hủy thì không báo lỗi
    if (error !== 'cancel') {
      ElMessage.error('Xóa thất bại!');
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
.student-list-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.table-wrapper {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}
</style>
