<template>
  <el-form :model="localCourse" label-width="120px">
    <!-- Error messages từ backend -->
    <el-alert
      v-if="formErrors.length > 0"
      title="Lỗi khi lưu"
      type="error"
      :closable="true"
      style="margin-bottom: 16px"
    >
      <ul style="margin: 0; padding-left: 20px">
        <li v-for="(err, idx) in formErrors" :key="idx">{{ err }}</li>
      </ul>
    </el-alert>

    <el-form-item label="Tên khóa học" :rules="[{ required: true, message: 'Tên không được để trống', trigger: 'blur' }]">
      <el-input v-model="localCourse.name" placeholder="Nhập tên khóa học" />
    </el-form-item>

    <el-form-item label="Mã khóa học">
      <el-input v-model="localCourse.code" placeholder="Nhập mã khóa học" />
    </el-form-item>

    <el-form-item label="Mô tả">
      <el-input
        type="textarea"
        v-model="localCourse.description"
        placeholder="Nhập mô tả khóa học"
        :rows="3"
      />
    </el-form-item>

    <!-- Gallery: hiển thị tất cả thumbnails cũ (nếu detail) -->
    <el-form-item label="Ảnh hiện tại" v-if="isEdit && existingThumbnails.length > 0">
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
            Xóa
          </span>
        </div>
      </div>
    </el-form-item>

     <!-- Upload Thumbnail: preview cục bộ, không auto upload -->
     <el-form-item label="Ảnh đại diện mới">
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
       <p style="color: #999; font-size: 12px; margin-top: 8px">Upload ảnh mới sẽ thay thế toàn bộ ảnh cũ</p>
     </el-form-item>

     <el-form-item>
       <el-button type="primary" @click="submitForm">Lưu</el-button>
       <el-button @click="cancelForm">Hủy</el-button>
     </el-form-item>
   </el-form>
 </template>

 <script setup>
 import { ref, watch } from "vue";
 import { ElMessage } from "element-plus";

 const props = defineProps({
   course: {
     type: Object,
     default: () => ({
       id: null,
      name: "",
      code: "",
      description: "",
      status: "ACTIVE",
      thumbnail: [],
    }),
  },
  isEdit: { type: Boolean, default: false },
  errors: { type: Array, default: () => [] }
});
const emit = defineEmits(["save", "cancel"]);
const formErrors = ref([]);
 const localCourse = ref({
   id: props.course?.id || null,
   name: props.course?.name || "",
   code: props.course?.code || "",
   description: props.course?.description || "",
   status: props.course?.status || "1",
   thumbnailFile: null,
 });
 const existingThumbnails = ref([]); // list ảnh hiện tại từ backend
 const deletedThumbnailIds = ref([]); // list id ảnh bị đánh dấu xóa
 const newThumbnailPreview = ref(""); // preview ảnh mới upload
 const uploadList = ref([]);

 watch(
   () => props.course,
   (newVal) => {
     if (newVal) {
       localCourse.value = {
         id: newVal.id || null,
         name: newVal.name || "",
         code: newVal.code || "",
         description: newVal.description || "",
         status: newVal.status || "1",
         thumbnailFile: null,
       };
      // lưu danh sách thumbnail từ backend
      existingThumbnails.value = newVal.thumbnail || [];
      deletedThumbnailIds.value = [];
      newThumbnailPreview.value = "";
      uploadList.value = [];
    }
  },
  { deep: true, immediate: true }
);
const beforeUpload = (file) => {
  localCourse.value.thumbnailFile = file;
  // khi upload ảnh mới, mark toàn bộ ảnh cũ để xóa
  deletedThumbnailIds.value = existingThumbnails.value.map(t => t.id);
  const reader = new FileReader();
  reader.onload = (e) => {
    newThumbnailPreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
  uploadList.value = [{ name: file.name }];
  return false;
};
const onRemove = () => {
  localCourse.value.thumbnailFile = null;
  if (newThumbnailPreview.value && newThumbnailPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(newThumbnailPreview.value);
  }
  newThumbnailPreview.value = "";
  uploadList.value = [];
  deletedThumbnailIds.value = [];
}

// Xóa 1 thumbnail từ list
const deleteThumbnailItem = (thumbId) => {
  if (!deletedThumbnailIds.value.includes(thumbId)) {
    deletedThumbnailIds.value.push(thumbId);
  } else {
    deletedThumbnailIds.value = deletedThumbnailIds.value.filter(id => id !== thumbId);
  }
};
const submitForm = () => {
   if (!localCourse.value.name || !localCourse.value.name.trim()) {
     ElMessage.error("Tên khóa học không được để trống");
     return;
   }
   emit("save", {
     course: { ...localCourse.value },
     thumbnailFile: localCourse.value.thumbnailFile,
     deleteThumbnailIds: deletedThumbnailIds.value,
   });
 };

 const cancelForm = () => {
   emit("cancel");
 };

// watch errors từ parent
watch(
  () => props.errors,
  (newErrors) => {
    formErrors.value = Array.isArray(newErrors) ? newErrors : [];
  },
  { immediate: true }
);
</script>

<style scoped>
</style>
