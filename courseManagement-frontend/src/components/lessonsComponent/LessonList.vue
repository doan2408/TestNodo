<template>
  <div>
    <!-- Toolbar -->
    <div
      style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap;"
    >
      <el-button type="primary" @click="createLesson">{{ t('lesson.addNew') }}</el-button>
      <el-input
        v-model="keyword"
        :placeholder="t('lesson.searchPlaceholder')"
        style="width: 220px"
        @input="searchLessons"
        clearable
      />
      <span
        v-if="props.courseId"
        style="color: #909399; font-size: 12px; margin-top: 4px; display: block"
      >
        {{ t('lesson.course') }}: {{ getCourseTitle(props.courseId) }}
      </span>
      <span></span>
    </div>

    <!-- Table -->
    <el-table :data="lessons" style="width: 100%">
      <el-table-column label="STT" width="60">
        <template #default="scope">
          {{ (props.page - 1) * props.size + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column :label="t('lesson.lessonTitle')" prop="title" with="120" />

      <!-- Video preview -->
      <el-table-column :label="t('lesson.video')" width="180">
  <template #default="{ row }">
    <div v-if="row.videos?.length > 0">
      <div v-for="(video, index) in row.videos" :key="index">
        <a
          :href="video.url"
          target="_blank"
          style="color: #409eff; text-decoration: underline; font-size: 12px;"
        >
          {{ video.fileName || t('lesson.viewVideo') }}
        </a>
        <span
          v-if="index < row.videos.length - 1"
          style="color: #999; font-size: 11px"
          >,</span
        >
      </div>
    </div>
    <span v-else style="color: #ccc">{{ t('lesson.noVideo') }}</span>
  </template>
</el-table-column>

      <!-- Thumbnail preview -->
      <el-table-column :label="t('lesson.thumbnail')" width="180">
        <template #default="{ row }">
          <el-image
            v-if="row.thumbnails?.length > 0"
            :src="row.thumbnails[0].url"
            style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer;"
            :preview-src-list="row.thumbnails.map((t) => t.url)"
            :initial-index="0"
          />
          <span v-else style="color: #ccc">{{ t('lesson.noThumbnail') }}</span>
        </template>
      </el-table-column>

      <el-table-column :label="t('common.action')" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" @click="onDelete(row.id)">
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div style="margin-top: 20px; text-align: right">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalLessons"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n"; // Import the useI18n function
import { getAllLessons } from "@/api/LessonService";

// Initialize i18n
const { t } = useI18n();

const lessons = ref([]);
const keyword = ref("");
const totalLessons = ref(0);

const props = defineProps({
  courseId: { type: Number, required: true },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  courses: { type: Array, default: () => [] },
});

const getCourseTitle = (courseId) => {
  const course = props.courses.find((c) => c.id === courseId);
  return course
    ? course.title || course.name || `${t('lesson.course')} ${courseId}`
    : `${t('lesson.course')} ID: ${courseId}`;
};

const emit = defineEmits(["create", "edit", "delete", "pageChange"]);

const loadLessons = async () => {
  try {
    const response = await getAllLessons({
      courseId: props.courseId,
      keyword: keyword.value,
      page: props.page - 1,
      size: props.size,
    });
    lessons.value = response.content || [];
    totalLessons.value = response.totalElements || 0;
  } catch (error) {
    console.error("Lỗi khi tải bài học:", error);
    ElMessage.error(t('lesson.saveFailed'));
    lessons.value = [];
    totalLessons.value = 0;
  }
};

watch(() => [props.courseId, props.page, props.size], loadLessons, {
  immediate: true,
});

const searchTimeout = ref(null);

const searchLessons = () => {
  if (searchTimeout.value) clearTimeout(searchTimeout.value);
  searchTimeout.value = setTimeout(() => {
    emit("pageChange", 1); // reset page =1
    loadLessons();
  }, 500);
};

const createLesson = () => emit("create");

const onEdit = (row) => emit("edit", row);

const onDelete = (id) => emit("delete", id);

const onPageChange = (newPage) => {
  emit("pageChange", newPage);
};

defineExpose({
  loadLessons,
});
</script>

<style scoped>
/* Container & Toolbar */
div {
  box-sizing: border-box;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

/* Table wrapper */
.el-table {
  width: 100% !important;
  min-width: 600px; /* tránh table bị co quá nhỏ */
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

/* Table headers */
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
}

/* Video / Thumbnail links */
.el-table a {
  color: #409eff;
  text-decoration: underline;
  font-size: 12px;
}

.el-table span {
  font-size: 11px;
  color: #999;
}

/* Thumbnail images */
.el-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

/* Action buttons */
.el-table .el-button {
  margin-right: 6px;
  font-size: 12px;
  padding: 2px 8px;
}

/* Pagination */
div[style*="margin-top: 20px"] {
  display: flex;
  justify-content: flex-end;
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
  }

  .el-table .el-button {
    margin-right: 4px;
    font-size: 11px;
    padding: 2px 6px;
  }

  /* Toolbar wrap */
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar input {
    width: 100% !important;
  }
}

</style>
