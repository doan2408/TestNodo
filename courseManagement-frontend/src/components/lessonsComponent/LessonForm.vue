<template>
  <el-form :model="localLesson" label-width="120px">
    <!-- Error alert -->
    <el-alert
      v-if="formErrors.length > 0"
      title="Lỗi khi lưu"
      type="error"
      :closable="true"
      style="margin-bottom:16px"
    >
      <ul style="margin:0;padding-left:20px">
        <li v-for="(err, idx) in formErrors" :key="idx">{{ err }}</li>
      </ul>
    </el-alert>

    <!-- Chọn Khóa học -->
    <el-form-item 
      label="Khóa học" 
      :rules="[{ required: true, message: 'Vui lòng chọn khóa học', trigger: 'change' }]"
    >
      <el-select 
        v-model="localLesson.courseId" 
        placeholder="Chọn khóa học"
        style="width: 100%"
        filterable
        clearable
        :loading="props.courses.length === 0"
        :disabled="!isEdit"
      >
        <el-option
          v-for="course in props.courses"
          :key="course.id"
          :label="`${course.name} - ${course.code}`"
          :value="course.id"
        >

        </el-option>
      </el-select>
      <span v-if="!isEdit" style="color: #909399; font-size: 12px; margin-top: 4px; display: block">
        🔒 Đang thêm bài học vào khóa: {{ getCourseTitle(props.courseId) }}
      </span>
      <span v-else-if="isEdit && localLesson.courseId" style="color: #409eff; font-size: 12px; margin-top: 4px; display: block">
        💡 Có thể thay đổi khóa học cho bài học này
      </span>
    </el-form-item>

    <!-- Tiêu đề -->
    <el-form-item 
      label="Tiêu đề" 
      :rules="[{ required: true, message: 'Tiêu đề không được để trống', trigger: 'blur' }]"
    >
      <el-input v-model="localLesson.title" placeholder="Nhập tiêu đề bài học" />
    </el-form-item>

    <!-- Video hiện tại -->
    <el-form-item label="Video hiện tại" v-if="existingVideos.length > 0">
      <div style="display:flex;gap:12px;flex-wrap:wrap">
        <div 
          v-for="video in existingVideos" 
          :key="video.id" 
          style="position:relative;width:120px;padding:8px;border:1px solid #ddd;border-radius:4px" 
          :class="{ 'deleted': deletedVideoIds.includes(video.id) }"
        >
          <p style="margin:0;font-size:12px;word-break:break-all">{{ video.fileName || 'Video' }}</p>
          <el-button 
            size="small" 
            :type="deletedVideoIds.includes(video.id) ? 'success' : 'danger'" 
            style="margin-top:4px;width:100%" 
            @click="toggleDeleteVideo(video.id)"
          >
            {{ deletedVideoIds.includes(video.id) ? '✓ Khôi phục' : '✕ Xóa' }}
          </el-button>
        </div>
      </div>
    </el-form-item>

    <!-- Upload video -->
    <el-form-item label="Video mới">
      <el-upload 
        drag 
        :file-list="uploadVideoList" 
        :before-upload="beforeUploadVideo" 
        :on-remove="onRemoveVideo" 
        accept="video/*" 
        :limit="1"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">Drop video or <em>click to upload</em></div>
      </el-upload>
    </el-form-item>

    <!-- Thumbnail hiện tại -->
    <el-form-item label="Thumbnail hiện tại" v-if="existingThumbnails.length > 0">
      <div style="display:flex;gap:12px;flex-wrap:wrap">
        <div 
          v-for="thumb in existingThumbnails" 
          :key="thumb.id" 
          style="position:relative;width:80px;height:80px" 
          :class="{ 'deleted': deletedThumbnailIds.includes(thumb.id) }"
        >
          <el-image 
            :src="thumb.url" 
            style="width:100%;height:100%;object-fit:cover;border-radius:4px;" 
            :class="{ 'opacity-50': deletedThumbnailIds.includes(thumb.id) }" 
          />
          <el-button 
            size="small" 
            :type="deletedThumbnailIds.includes(thumb.id) ? 'success' : 'danger'" 
            style="position:absolute;top:2px;right:2px;padding:2px 4px;font-size:10px;height:20px;line-height:1" 
            @click="toggleDeleteThumbnail(thumb.id)"
          >
            {{ deletedThumbnailIds.includes(thumb.id) ? '✓' : '✕' }}
          </el-button>
        </div>
      </div>
    </el-form-item>

    <!-- Upload thumbnail -->
    <el-form-item label="Thumbnail mới">
      <el-upload 
        list-type="picture-card" 
        :file-list="uploadThumbnailList" 
        :before-upload="beforeUploadThumbnail" 
        :on-remove="onRemoveThumbnail" 
        accept="image/*" 
        :limit="1"
      >
        <i v-if="!thumbnailPreview">+</i>
        <img v-else :src="thumbnailPreview" style="width:100%;height:100%;object-fit:cover" />
      </el-upload>
    </el-form-item>

    <!-- Submit -->
    <el-form-item>
      <el-button type="primary" @click="submitForm">Lưu</el-button>
      <el-button @click="cancelForm">Hủy</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { UploadFilled } from "@element-plus/icons-vue";

const props = defineProps({
  lesson: { type: Object, default: null },
  courseId: { type: Number, default: null },
  isEdit: { type: Boolean, default: false },
  errors: { type: Array, default: () => [] },
  courses: { type: Array, default: () => [] },
});

const emit = defineEmits(["save", "cancel"]);

const defaultLesson = () => ({ 
  id: null, 
  courseId: props.courseId || null, 
  title: "", 
  videos: [], 
  thumbnails: [] 
});

const localLesson = ref({ ...defaultLesson() });
const existingVideos = ref([]);
const existingThumbnails = ref([]);
const deletedVideoIds = ref([]);
const deletedThumbnailIds = ref([]);
const videoFile = ref(null);
const thumbnailFile = ref(null);
const uploadVideoList = ref([]);
const uploadThumbnailList = ref([]);
const thumbnailPreview = ref("");
const formErrors = ref([]);

watch(
  () => props.lesson,
  (newVal) => {
    if (!newVal) {
      localLesson.value = { ...defaultLesson() };
      existingVideos.value = [];
      existingThumbnails.value = [];
      deletedVideoIds.value = [];
      deletedThumbnailIds.value = [];
      videoFile.value = null;
      thumbnailFile.value = null;
      uploadVideoList.value = [];
      uploadThumbnailList.value = [];
      thumbnailPreview.value = "";
      return;
    }
    
    // Lấy courseId và đảm bảo là number
    let courseIdValue = null;
    if (newVal.courseId) {
      courseIdValue = Number(newVal.courseId);
    } else if (newVal.course?.id) {
      courseIdValue = Number(newVal.course.id);
    } else if (props.courseId) {
      courseIdValue = Number(props.courseId);
    }
    
    localLesson.value = { 
      id: newVal.id || null, 
      courseId: courseIdValue,
      title: newVal.title || "" 
    };
    existingVideos.value = newVal.videos || [];
    existingThumbnails.value = newVal.thumbnails || [];
    deletedVideoIds.value = [];
    deletedThumbnailIds.value = [];
    videoFile.value = null;
    thumbnailFile.value = null;
    uploadVideoList.value = [];
    uploadThumbnailList.value = [];
    thumbnailPreview.value = "";
  },
  { deep: true, immediate: true }
);

watch(
  () => props.errors,
  (newErrors) => {
    formErrors.value = Array.isArray(newErrors) ? newErrors : [];
  },
  { immediate: true }
);

const beforeUploadVideo = (file) => {
  videoFile.value = file;
  uploadVideoList.value = [{ name: file.name }];
  return false;
};

const onRemoveVideo = () => {
  videoFile.value = null;
  uploadVideoList.value = [];
};

const beforeUploadThumbnail = (file) => {
  thumbnailFile.value = file;
  deletedThumbnailIds.value = existingThumbnails.value.map(t => t.id);
  const reader = new FileReader();
  reader.onload = (e) => {
    thumbnailPreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
  uploadThumbnailList.value = [{ name: file.name }];
  return false;
};

const onRemoveThumbnail = () => {
  thumbnailFile.value = null;
  if (thumbnailPreview.value?.startsWith("blob:")) {
    URL.revokeObjectURL(thumbnailPreview.value);
  }
  thumbnailPreview.value = "";
  uploadThumbnailList.value = [];
  deletedThumbnailIds.value = [];
};

const toggleDeleteVideo = (id) => {
  if (!deletedVideoIds.value.includes(id)) {
    deletedVideoIds.value.push(id);
  } else {
    deletedVideoIds.value = deletedVideoIds.value.filter(vid => vid !== id);
  }
};

const toggleDeleteThumbnail = (id) => {
  if (!deletedThumbnailIds.value.includes(id)) {
    deletedThumbnailIds.value.push(id);
  } else {
    deletedThumbnailIds.value = deletedThumbnailIds.value.filter(tid => tid !== id);
  }
};

const getCourseTitle = (courseId) => {
  const course = props.courses.find(c => c.id === courseId);
  return course ? (course.title || course.name || `Course ${courseId}`) : `ID: ${courseId}`;
};

const submitForm = () => {
  if (!localLesson.value.courseId) {
    ElMessage.error("Vui lòng chọn khóa học");
    return;
  }
  if (!localLesson.value.title || !localLesson.value.title.trim()) {
    ElMessage.error("Tiêu đề bài học không được để trống");
    return;
  }
  emit("save", {
    lesson: { 
      id: localLesson.value.id, 
      courseId: localLesson.value.courseId,
      title: localLesson.value.title 
    },
    videoFile: videoFile.value,
    thumbnailFile: thumbnailFile.value,
    deleteVideoIds: deletedVideoIds.value,
    deleteThumbnailIds: deletedThumbnailIds.value,
  });
};

const cancelForm = () => {
  emit("cancel");
};
</script>

<style scoped>
.deleted { opacity: 0.5; }
.opacity-50 { opacity: 0.5; }
</style>