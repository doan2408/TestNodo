<template>
  <el-form :model="localCourse" label-width="120px">
    <!-- Error messages -->
    <el-alert
      v-if="formErrors.length > 0"
      :title="t('common.error')"
      type="error"
      closable
      style="margin-bottom: 16px"
    >
      <ul style="margin: 0; padding-left: 20px">
        <li v-for="(err, idx) in formErrors" :key="idx">{{ err }}</li>
      </ul>
    </el-alert>

    <el-form-item
      :label="t('course.name')"
      :rules="[{ required: true, message: t('course.nameRequired'), trigger: 'blur' }]"
    >
      <el-input v-model="localCourse.name" :placeholder="t('course.name')" />
    </el-form-item>

    <el-form-item :label="t('course.code')">
      <el-input v-model="localCourse.code" :placeholder="t('course.code')" />
    </el-form-item>

    <el-form-item :label="t('course.description')">
      <el-input
        type="textarea"
        v-model="localCourse.description"
        :placeholder="t('course.description')"
        :rows="3"
      />
    </el-form-item>

    <!-- Existing thumbnails -->
    <el-form-item :label="t('course.thumbnail')" v-if="isEdit && existingThumbnails.length > 0">
      <div style="display: flex; gap: 8px; flex-wrap: wrap">
        <div
          v-for="thumb in existingThumbnails"
          :key="thumb.id"
          style="position: relative; width: 80px; height: 80px"
        >
          <el-image
            :src="thumb.url"
            style="width: 100%; height: 100%; object-fit: cover; border-radius: 4px"
          />
          <el-button
            v-if="!deletedThumbnailIds.includes(thumb.id)"
            size="small"
            type="danger"
            style="position: absolute; top: 0; right: 0; padding: 2px 4px; font-size: 10px"
            @click="deleteThumbnailItem(thumb.id)"
          >
            ✕
          </el-button>
          <span
            v-else
            style="position: absolute; top: 0; right: 0; background: #f56c6c; color: white; padding: 2px 4px; font-size: 10px; border-radius: 2px"
          >
            {{t('common.delete')}}
          </span>
        </div>
      </div>
    </el-form-item>

    <!-- Upload thumbnail -->
    <el-form-item :label="t('course.thumbnail')">
      <el-upload
        list-type="picture-card"
        :file-list="uploadList"
        :before-upload="beforeUpload"
        :on-remove="onRemove"
        accept="image/*"
        :limit="1"
      >
        <i v-if="!newThumbnailPreview">+</i>
        <img v-else :src="newThumbnailPreview" style="width:100%;height:100%;object-fit:cover" />
      </el-upload>
      <p style="color: #999; font-size: 12px; margin-top: 8px">
        {{t('course.uploadTip', 'Upload new image will replace old ones')}}
      </p>
    </el-form-item>

    <!-- Submit -->
    <el-form-item>
      <el-button type="primary" @click="submitForm">{{t('common.save')}}</el-button>
      <el-button @click="cancelForm">{{t('common.cancel')}}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";

const props = defineProps({
  course: { type: Object, default: () => ({ id: null, name: "", code: "", description: "", status: "1", thumbnail: [] }) },
  isEdit: { type: Boolean, default: false },
  errors: { type: Array, default: () => [] }
});
const emit = defineEmits(["save", "cancel"]);

import { useI18n } from "vue-i18n";

const { t } = useI18n();

const formErrors = ref([]);
const localCourse = ref({ ...props.course, thumbnailFile: null });
const existingThumbnails = ref([]);
const deletedThumbnailIds = ref([]);
const newThumbnailPreview = ref("");
const uploadList = ref([]);

watch(() => props.course, (newVal) => {
  if (newVal) {
    localCourse.value = { ...newVal, thumbnailFile: null };
    existingThumbnails.value = newVal.thumbnail || [];
    deletedThumbnailIds.value = [];
    newThumbnailPreview.value = "";
    uploadList.value = [];
  }
}, { deep: true, immediate: true });

watch(() => props.errors, (newErrors) => {
  formErrors.value = Array.isArray(newErrors) ? newErrors : [];
}, { immediate: true });

const beforeUpload = (file) => {
  localCourse.value.thumbnailFile = file;
  deletedThumbnailIds.value = existingThumbnails.value.map(t => t.id);
  const reader = new FileReader();
  reader.onload = (e) => newThumbnailPreview.value = e.target.result;
  reader.readAsDataURL(file);
  uploadList.value = [{ name: file.name }];
  return false;
};

const onRemove = () => {
  localCourse.value.thumbnailFile = null;
  newThumbnailPreview.value = "";
  uploadList.value = [];
  deletedThumbnailIds.value = [];
};

const deleteThumbnailItem = (id) => {
  if (!deletedThumbnailIds.value.includes(id)) {
    deletedThumbnailIds.value.push(id);
  } else {
    deletedThumbnailIds.value = deletedThumbnailIds.value.filter(i => i !== id);
  }
};

const submitForm = () => {
  if (!localCourse.value.name || !localCourse.value.name.trim()) {
    ElMessage.error(t('course.nameRequired'));
    return;
  }
  emit("save", { 
    course: { ...localCourse.value },
    thumbnailFile: localCourse.value.thumbnailFile,
    deleteThumbnailIds: deletedThumbnailIds.value
  });
};

const cancelForm = () => emit("cancel");
</script>

<style scoped>
</style>
