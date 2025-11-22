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
        <el-table-column :label="t('common.action')" width="340">
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
