<template>
  <div class="course-list-container">
    <div class="table-wrapper">
      <el-table :data="data" style="width: 100%" :max-height="tableMaxHeight">
        <el-table-column :label="t('common.stt')" width="60">
          <template #default="scope">
            {{ page * size + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- Thumbnail -->
        <el-table-column :label="t('course.thumbnail')" width="80" v-if="hasThumbnail">
          <template #default="scope">
            <el-image
              v-if="scope.row.thumbnail?.[0]?.url"
              :src="scope.row.thumbnail[0].url"
              style="width:50px;height:50px;border-radius:4px;object-fit:cover;"
            />
            <span v-else style="color:#999;font-size:12px">{{t('common.none')}}</span>
          </template>
        </el-table-column>

        <!-- Dynamic columns -->
        <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" :width="col.width">
          <template #default="scope" v-if="col.prop === 'status'">
            <el-tag type="success" v-if="scope.row.status == '1'">{{t('course.active')}}</el-tag>
            <el-tag type="danger" v-else>{{t('course.inactive')}}</el-tag>
          </template>
        </el-table-column>

        <!-- Actions -->
        <el-table-column :label="t('common.action')" width="420">
          <template #default="scope">
            <el-button size="small" @click="$emit('edit', scope.row)">{{t('common.edit')}}</el-button>
            <el-button size="small" @click="$emit('viewStudents', scope.row.id)">{{t('student.title')}}</el-button>
            <el-button size="small" type="warning" @click="$emit('viewLessons', scope.row.id)">{{t('course.viewLessons')}}</el-button>
            <el-button size="small" type="danger" @click="confirmDeleteCourse(scope.row.id)">{{t('common.delete')}}</el-button>
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

const emit = defineEmits(["edit", "delete", "viewLessons", "viewStudents", "pageChange"]);

const tableMaxHeight = ref("60vh");
const hasThumbnail = computed(() => props.data.some(i => i.thumbnail));

const updateTableHeight = () => {
  const headerEstimated = 180;
  const available = window.innerHeight - headerEstimated;
  tableMaxHeight.value = `${Math.max(300, available)}px`;
};

const loadCourses = async () => {
  try { await props.loadDataFn(); } 
  catch (err) { console.error(err); }
};

watch(() => [props.keyword, props.page, props.size], () => loadCourses(), { immediate: true });
defineExpose({ loadCourses });

const changePage = (p) => emit("pageChange", p - 1);

const confirmDeleteCourse = async (id) => {
  if (!props.deleteFn) {
    ElMessage.warning(t('common.deleteConfirm'));
    return;
  }
  try {
    await ElMessageBox.confirm(
      t('common.deleteConfirm'),
      t('common.confirm'),
      { confirmButtonText: t('common.yes'), cancelButtonText: t('common.no'), type: "warning" }
    );
    await props.deleteFn(id);
    ElMessage.success(t('course.deleteSuccess'));
    loadCourses();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(t('course.deleteFailed'));
      console.error(err);
    }
  }
};

onMounted(() => {
  updateTableHeight();
  window.addEventListener("resize", updateTableHeight);
});
</script>

<style scoped>
/* Container padding */
.course-list-container {
  padding: 20px;
  box-sizing: border-box;
}

/* Table wrapper */
.table-wrapper {
  margin-bottom: 20px;
}

/* Table base */
.el-table {
  width: 100% !important;
  min-width: 600px; /* tránh cột co quá nhỏ */
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* Table header */
.el-table th {
  background-color: #f9fafb;
  color: #374151;
  font-weight: 600;
  padding: 12px 16px;
  white-space: nowrap;
}

/* Table cells */
.el-table td {
  padding: 10px 16px;
  vertical-align: middle;
  white-space: nowrap;
  overflow: visible;
  text-overflow: unset;
  font-size: 13px;
}

/* Thumbnail image */
.el-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
}

/* Tags */
.el-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
}

/* Action buttons cell - 2x2 grid */
.el-table-column .cell {
  display: grid !important;
  grid-template-columns: repeat(2, max-content); /* 2 nút trên 2 nút dưới */
  grid-auto-rows: max-content;
  gap: 6px;
  justify-content: start;
  align-items: center;
}

/* Nút action */
.el-table-column .cell .el-button {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* Responsive */
@media (max-width: 768px) {
  .el-table {
    min-width: 500px;
  }

  .el-table th,
  .el-table td {
    padding: 8px 12px;
    font-size: 12px;
  }

  .el-table-column .cell {
    grid-template-columns: repeat(2, max-content);
    gap: 4px;
  }

  .el-table-column .cell .el-button {
    font-size: 11px;
    padding: 2px 6px;
  }
}
</style>
