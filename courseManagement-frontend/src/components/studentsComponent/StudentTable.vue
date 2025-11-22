<template>
  <div class="student-list-container">
    <!-- Table -->
    <div class="table-wrapper">
      <el-table :data="data" style="width: 100%" :max-height="tableMaxHeight">
        <el-table-column :label="t('common.none')" width="60">
          <template #default="scope">
            {{ page * size + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- Avatar column (nếu data có avatar) -->
        <el-table-column :label="t('student.avatar')" width="80" v-if="hasAvatar">
          <template #default="scope">
            <img
              v-if="scope.row.avatar?.length"
              :src="scope.row.avatar[0].url"
              alt="avatar"
              style="
                width: 45px;
                height: 45px;
                border-radius: 50%;
                object-fit: cover;
              "
            />
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
          <!-- Custom render cho gender -->
          <template #default="scope" v-if="col.prop === 'gender'">
            <el-tag type="success" v-if="scope.row.gender === '1'">{{ t('student.male') }}</el-tag>
            <el-tag type="info" v-else>{{ t('student.female') }}</el-tag>
          </template>

          <!-- Custom render cho status -->
          <template #default="scope" v-else-if="col.prop === 'status'">
            <el-tag type="success" v-if="scope.row.status === '1'">{{ t('course.active') }}</el-tag>
            <el-tag type="danger" v-else>{{ t('course.inactive') }}</el-tag>
          </template>
        </el-table-column>

        <!-- Thao tác: Sửa / Xem / Xóa -->
        <el-table-column :label="t('common.action')" width="220">
          <template #default="scope">
            <el-button size="small" @click="$emit('edit', scope.row)">
              {{ t('common.edit') }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="confirmDeleteStudent(scope.row.id)"
            >
              {{ t('common.delete') }}
            </el-button>
            <el-button size="small" @click="$emit('detail', scope.row)">
              {{ t('common.view') || 'Xem' }}
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
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// props
const props = defineProps({
  courseId: { type: Number },
  keyword: { type: String, default: "" },
  gender: { type: String, default: "" },
  page: { type: Number, default: 0 },
  size: { type: Number, default: 10 },
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  totalElements: { type: Number, default: 0 },
  loadDataFn: { type: Function, required: true },
  deleteFn: { type: Function, default: null },
});

const emit = defineEmits(["edit", "delete", "detail", "pageChange"]);

// state
const tableMaxHeight = ref("60vh");

// Check nếu data có avatar
const hasAvatar = computed(() => {
  return props.data.some((item) => item.avatar);
});

// responsive table height
const updateTableHeight = () => {
  const headerEstimated = 180;
  const available = window.innerHeight - headerEstimated;
  tableMaxHeight.value = `${Math.max(300, available)}px`;
};

// load data function
const loadStudents = async () => {
  try {
    await props.loadDataFn();
  } catch (err) {
    console.error(err);
  }
};

// watch props changes
watch(
  () => [props.keyword, props.gender, props.page, props.size],
  () => {
    loadStudents();
  },
  { immediate: true }
);

// expose
defineExpose({ loadStudents });

// pagination
const changePage = (p) => emit("pageChange", p - 1);

// Xóa sinh viên với confirm Box
const confirmDeleteStudent = async (id) => {
  if (!props.deleteFn) {
    ElMessage.warning(t('common.none') || "Chức năng xóa chưa được cấu hình.");
    return;
  }

  try {
    await ElMessageBox.confirm(
      t('common.deleteConfirm'),
      t('common.confirm'),
      {
        confirmButtonText: t('common.yes'),
        cancelButtonText: t('common.no'),
        type: "warning",
      }
    );

    await props.deleteFn(id);
    ElMessage.success(t('student.deleteSuccess'));
    loadStudents();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(t('student.deleteFailed'));
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

.table-wrapper {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}
</style>
