<template>
  <div class="create-page">
    <div class="create-container">
      <div class="create-header">
        <h1>创建 Snippet</h1>
        <p>分享你的灵感、作品或故事</p>
      </div>

      <form class="create-form" @submit.prevent="handleSubmit">
        <!-- 图片上传 -->
        <div class="form-group">
          <label class="form-label">图片</label>
          <div
            class="upload-area"
            :class="{ dragging: isDragging }"
            @click="triggerFileInput"
            @dragenter.prevent="isDragging = true"
            @dragover.prevent
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
          >
            <PhImages :size="40" class="upload-icon" />
            <span class="upload-text">点击或拖拽上传图片</span>
            <span class="upload-hint">最多 9 张，单张不超过 5MB</span>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              multiple
              class="file-input"
              @change="handleFileChange"
            />
          </div>

          <div v-if="imageFiles.length > 0" class="preview-list">
            <div
              v-for="(file, index) in imageFiles"
              :key="file.preview"
              class="preview-item"
            >
              <img :src="file.preview" alt="" />
              <button
                type="button"
                class="remove-btn"
                @click="removeImage(index)"
              >
                <PhX :size="14" />
              </button>
            </div>
          </div>
          <span v-if="imageError" class="field-error">{{ imageError }}</span>
        </div>

        <!-- 标题 -->
        <div class="form-group">
          <label class="form-label" for="title">标题</label>
          <input
            id="title"
            v-model="title"
            type="text"
            placeholder="给你的帖子起个标题"
          />
          <span v-if="fieldErrors.title" class="field-error">
            {{ fieldErrors.title[0] }}
          </span>
        </div>

        <!-- 描述 -->
        <div class="form-group">
          <label class="form-label" for="description">描述</label>
          <textarea
            id="description"
            v-model="description"
            rows="6"
            placeholder="介绍一下你的想法..."
          ></textarea>
          <span v-if="fieldErrors.description" class="field-error">
            {{ fieldErrors.description[0] }}
          </span>
        </div>

        <!-- 标签 -->
        <div class="form-group">
          <label class="form-label" for="tags">标签</label>
          <div class="tag-input-wrapper" @click="focusTagInput">
            <span v-for="(tag, index) in tags" :key="tag" class="tag-chip">
              {{ tag }}
              <button type="button" @click.stop="removeTag(index)">
                <PhX :size="12" />
              </button>
            </span>
            <input
              ref="tagInputRef"
              v-model="tagInput"
              type="text"
              placeholder="输入后按回车添加"
              @keydown.enter.prevent="addTag"
              @keydown.space.prevent="addTag"
              @keydown.comma.prevent="addTag"
              @keydown.period.prevent="addTag"
              @keydown.backspace="handleTagBackspace"
            />
          </div>
          <span v-if="tagError" class="field-error">{{ tagError }}</span>
          <span class="tag-hint">最多 10 个标签，每个标签不超过 20 个字符</span>
        </div>

        <p v-if="submitError" class="submit-error">{{ submitError }}</p>

        <button type="submit" class="submit-btn" :disabled="submitting">
          {{ submitting ? "发布中..." : "发布" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { PhImages, PhX } from "@phosphor-icons/vue";
import { createPost } from "@/api/post";
import { validateCreatePostForm } from "@/utils/validators";

const router = useRouter();

const title = ref("");
const description = ref("");
const tags = ref([]);
const tagInput = ref("");
const tagInputRef = ref(null);
const fileInput = ref(null);
const imageFiles = ref([]);
const imageError = ref("");
const tagError = ref("");
const fieldErrors = ref({});
const submitError = ref("");
const submitting = ref(false);
const isDragging = ref(false);

const MAX_IMAGES = 9;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
const MAX_TAGS = 10;
const MAX_TAG_LENGTH = 20;

function triggerFileInput() {
  fileInput.value?.click();
}

function validateImageFile(file) {
  if (!file.type.startsWith("image/")) {
    return "请选择图片文件";
  }
  if (file.size > MAX_IMAGE_SIZE) {
    return "单张图片不能超过 5MB";
  }
  return "";
}

function addFiles(files) {
  imageError.value = "";
  const remainingSlots = MAX_IMAGES - imageFiles.value.length;
  if (remainingSlots <= 0) {
    imageError.value = `最多上传 ${MAX_IMAGES} 张图片`;
    return;
  }

  const filesToAdd = Array.from(files).slice(0, remainingSlots);
  for (const file of filesToAdd) {
    const error = validateImageFile(file);
    if (error) {
      imageError.value = error;
      return;
    }
    imageFiles.value.push({
      file,
      preview: URL.createObjectURL(file),
    });
  }
}

function handleFileChange(event) {
  addFiles(event.target.files);
  event.target.value = "";
}

function handleDrop(event) {
  isDragging.value = false;
  addFiles(event.dataTransfer.files);
}

function removeImage(index) {
  URL.revokeObjectURL(imageFiles.value[index].preview);
  imageFiles.value.splice(index, 1);
  imageError.value = "";
}

function focusTagInput() {
  tagInputRef.value?.focus();
}

function addTag() {
  tagError.value = "";
  const raw = tagInput.value.trim().replace(/[,，]/g, "");
  if (!raw) {
    tagInput.value = "";
    return;
  }
  if (tags.value.length >= MAX_TAGS) {
    tagError.value = `最多添加 ${MAX_TAGS} 个标签`;
    return;
  }
  if (raw.length > MAX_TAG_LENGTH) {
    tagError.value = `单个标签不能超过 ${MAX_TAG_LENGTH} 个字符`;
    return;
  }
  if (tags.value.includes(raw)) {
    tagError.value = "标签已存在";
    return;
  }
  tags.value.push(raw);
  tagInput.value = "";
}

function removeTag(index) {
  tags.value.splice(index, 1);
  tagError.value = "";
}

function handleTagBackspace() {
  if (!tagInput.value && tags.value.length > 0) {
    tags.value.pop();
    tagError.value = "";
  }
}

async function handleSubmit() {
  fieldErrors.value = {};
  submitError.value = "";
  imageError.value = "";
  tagError.value = "";

  const errors = validateCreatePostForm({
    title: title.value,
    description: description.value,
  });
  if (errors) {
    fieldErrors.value = errors;
    return;
  }

  submitting.value = true;
  try {
    await createPost({
      title: title.value,
      description: description.value,
      tags: tags.value,
      images: imageFiles.value.map((item) => item.file),
    });
    router.push({ name: "Home" });
  } catch (e) {
    submitError.value = e.message;
  } finally {
    submitting.value = false;
  }
}

onUnmounted(() => {
  imageFiles.value.forEach((item) => {
    URL.revokeObjectURL(item.preview);
  });
});
</script>

<style src="@/style/create.less"></style>
