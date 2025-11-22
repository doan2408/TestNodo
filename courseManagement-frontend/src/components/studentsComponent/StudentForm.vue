<template>
  <el-form :model="localStudent" label-width="120px">
    <!-- Tên Sinh viên -->
    <el-form-item
      :label="t('student.name')"
      :rules="[
        { required: true, message: t('student.nameRequired'), trigger: 'blur' },
      ]"
    >
      <el-input v-model="localStudent.name" :placeholder="t('student.name')" />
    </el-form-item>

    <!-- Giới tính -->
    <el-form-item :label="t('student.gender')">
      <el-radio-group v-model="localStudent.gender">
        <el-radio :label="'1'">{{ t('student.male') }}</el-radio>
        <el-radio :label="'0'">{{ t('student.female') }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- Email -->
    <el-form-item
      :label="t('student.email')"
      :rules="[
        {
          required: true,
          type: 'email',
          message: t('student.emailRequired'),
          trigger: 'blur',
        },
      ]"
    >
      <el-input v-model="localStudent.email" :placeholder="t('student.email')" />
    </el-form-item>

    <!-- Điện thoại -->
    <el-form-item
      :label="t('student.phone')"
      :rules="[
        {
          required: true,
          message: t('common.required'),
          trigger: 'blur',
        },
      ]"
    >
      <el-input v-model="localStudent.phone" :placeholder="t('student.phone')" />
    </el-form-item>

    <!-- Avatar hiện tại: gallery với nút xóa/khôi phục -->
    <el-form-item :label="t('student.avatar')" v-if="existingAvatars.length > 0">
      <div style="display: flex; gap: 12px; flex-wrap: wrap">
        <div
          v-for="avatar in existingAvatars"
          :key="avatar.id"
          style="position: relative; width: 80px; height: 80px"
          :class="{ 'avatar-deleted': deletedAvatarIds.includes(avatar.id) }"
        >
          <el-image
            :src="avatar.url"
            style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              border-radius: 4px;
              transition: opacity 0.3s;
            "
            :class="{
              'avatar-deleted-img': deletedAvatarIds.includes(avatar.id),
            }"
          />
          <el-button
            size="small"
            :type="deletedAvatarIds.includes(avatar.id) ? 'success' : 'danger'"
            style="
              position: absolute;
              top: 2px;
              right: 2px;
              padding: 2px 4px;
              font-size: 10px;
              height: 20px;
              line-height: 1;
            "
            @click="toggleDeleteAvatar(avatar.id)"
          >
            {{ deletedAvatarIds.includes(avatar.id) ? "✓" : "✕" }}
          </el-button>
          <span
            v-if="deletedAvatarIds.includes(avatar.id)"
            style="
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              background: rgba(0, 0, 0, 0.6);
              color: white;
              padding: 4px 8px;
              border-radius: 4px;
              font-size: 12px;
            "
          >
            {{ t('common.deleteConfirm') }}
          </span>
        </div>
      </div>
    </el-form-item>

    <!-- Upload avatar mới (chỉ 1 file) -->
    <el-form-item :label="t('student.avatar')" v-if="!props.isRead">
      <el-upload
        list-type="picture-card"
        :file-list="uploadList"
        :before-upload="beforeUpload"
        :on-remove="onRemove"
        accept="image/*"
        :limit="1"
      >
        <i v-if="!newAvatarPreview">+</i>
        <img
          v-else
          :src="newAvatarPreview"
          style="width: 100%; height: 100%; object-fit: cover"
        />
      </el-upload>
      <p style="color: #999; font-size: 12px; margin-top: 8px">
        {{ t('student.avatar') }}
      </p>
    </el-form-item>

    <!-- Error messages từ backend -->
    <el-alert
      v-if="formErrors.length > 0"
      :title="t('common.error')"
      type="error"
      :closable="true"
      style="margin-bottom: 16px"
    >
      <ul style="margin: 0; padding-left: 20px">
        <li v-for="(err, idx) in formErrors" :key="idx">{{ err }}</li>
      </ul>
    </el-alert>

    <!-- Submit -->
    <el-form-item v-if="!props.isRead">
      <el-button type="primary" @click="submitForm" :loading="loading">
        {{ t('common.save') }}
      </el-button>
      <el-button @click="cancelEdit">{{ t('common.cancel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";

const { t } = useI18n();

const props = defineProps({
  student: { type: Object, default: null },
  isEdit: { type: Boolean, default: false },
  isRead: { type: Boolean, default: false },
  errors: { type: Array, default: () => [] },
});

const emit = defineEmits(["save", "cancel"]);

const defaultStudent = () => ({
  id: null,
  name: "",
  gender: "1",
  email: "",
  phone: "",
  avatar: [],
});

const localStudent = ref({ ...(props.student || defaultStudent()), avatarFile: null });
const existingAvatars = ref(props.student?.avatar || []);
const deletedAvatarIds = ref([]);
const newAvatarPreview = ref("");
const uploadList = ref([]);
const loading = ref(false);
const formErrors = ref([]);

// Watch student prop
watch(
  () => props.student,
  (newVal) => {
    if (!newVal) {
      localStudent.value = { ...defaultStudent(), avatarFile: null };
      existingAvatars.value = [];
      deletedAvatarIds.value = [];
      newAvatarPreview.value = "";
      uploadList.value = [];
      return;
    }
    localStudent.value = { ...newVal, avatarFile: null };
    existingAvatars.value = newVal.avatar || [];
    deletedAvatarIds.value = [];
    newAvatarPreview.value = "";
    uploadList.value = [];
    formErrors.value = [];
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

const beforeUpload = (file) => {
  localStudent.value.avatarFile = file;
  deletedAvatarIds.value = existingAvatars.value.map((a) => a.id);
  const reader = new FileReader();
  reader.onload = (e) => (newAvatarPreview.value = e.target.result);
  reader.readAsDataURL(file);
  uploadList.value = [{ name: file.name }];
  return false;
};

const onRemove = () => {
  localStudent.value.avatarFile = null;
  newAvatarPreview.value = "";
  uploadList.value = [];
  deletedAvatarIds.value = [];
};

const toggleDeleteAvatar = (id) => {
  if (!deletedAvatarIds.value.includes(id)) {
    deletedAvatarIds.value.push(id);
  } else {
    deletedAvatarIds.value = deletedAvatarIds.value.filter((aid) => aid !== id);
  }
};

const submitForm = () => {
  if (!localStudent.value.name || !localStudent.value.name.trim()) {
    ElMessage.error(t('student.nameRequired'));
    return;
  }
  if (!localStudent.value.email || !localStudent.value.email.trim()) {
    ElMessage.error(t('student.emailRequired'));
    return;
  }

  emit("save", {
    student: { ...localStudent.value },
    avatarFile: localStudent.value.avatarFile,
    deleteAvatarIds: deletedAvatarIds.value,
  });
};

const cancelEdit = () => emit("cancel");
</script>


<style scoped>
/* Highlight avatar bị đánh dấu xóa */
.avatar-deleted-img {
  opacity: 0.5;
}
</style>
