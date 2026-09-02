<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="avatar-uploader-overlay"
      @click.self="onClose"
      @keydown.esc="onClose"
    >
      <div class="avatar-uploader-modal">
        <header class="avatar-uploader-header">
          <h3 class="avatar-uploader-title">更换头像</h3>
          <button class="avatar-uploader-close" type="button" @click="onClose">
            <PhX :size="20" />
          </button>
        </header>

        <div
          v-if="!image"
          class="avatar-uploader-picker"
          @click="openFilePicker"
        >
          <PhImage :size="44" />
          <p class="avatar-uploader-picker-text">点击选择本地图片</p>
          <p class="avatar-uploader-picker-hint">
            支持 JPG / PNG / WebP 等常见格式
          </p>
        </div>

        <template v-else>
          <div
            class="avatar-uploader-viewport"
            :style="{ width: VIEWPORT + 'px', height: VIEWPORT + 'px' }"
            @pointerdown="onPointerDown"
            @pointermove="onPointerMove"
            @pointerup="onPointerUp"
            @pointercancel="onPointerUp"
            @wheel.prevent="onWheel"
          >
            <img
              :src="imageUrl"
              class="avatar-uploader-image"
              :style="imageStyle"
              draggable="false"
              alt=""
            />
            <div class="avatar-uploader-circle"></div>
          </div>

          <div class="avatar-uploader-zoom">
            <button
              class="avatar-uploader-zoom-btn"
              type="button"
              @click="setScale(scaleFactor - 0.2)"
            >
              <PhMinus :size="16" />
            </button>
            <input
              class="avatar-uploader-slider"
              type="range"
              :min="MIN_SCALE"
              :max="MAX_SCALE"
              step="0.01"
              :value="scaleFactor"
              @input="setScale(Number($event.target.value))"
            />
            <button
              class="avatar-uploader-zoom-btn"
              type="button"
              @click="setScale(scaleFactor + 0.2)"
            >
              <PhPlus :size="16" />
            </button>
          </div>
          <p class="avatar-uploader-tip">
            拖拽移动图片，滚动鼠标或拖动滑块调整大小
          </p>
        </template>

        <p v-if="errorMsg" class="avatar-uploader-error">{{ errorMsg }}</p>

        <footer class="avatar-uploader-footer">
          <button
            v-if="image"
            class="avatar-uploader-btn avatar-uploader-btn-secondary"
            type="button"
            @click="openFilePicker"
          >
            重新选择
          </button>
          <button
            class="avatar-uploader-btn avatar-uploader-btn-secondary"
            type="button"
            @click="onClose"
          >
            取消
          </button>
          <button
            class="avatar-uploader-btn avatar-uploader-btn-primary"
            type="button"
            :disabled="!image || uploading"
            @click="onConfirm"
          >
            {{ uploading ? "上传中..." : "确定" }}
          </button>
        </footer>

        <input
          ref="fileInput"
          class="avatar-uploader-file-input"
          type="file"
          accept="image/*"
          @change="onFileChange"
        />
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { PhX, PhImage, PhMinus, PhPlus } from "@phosphor-icons/vue";
import { updateUserAvatar } from "@/api/getUserInfo";

const props = defineProps({
  visible: Boolean,
});
const emit = defineEmits(["close", "uploaded"]);

// 裁剪视口尺寸（正方形），输出图片会是 2 倍大小保证清晰度
const VIEWPORT = 280;
const OUTPUT_SIZE = VIEWPORT * 2;
const MIN_SCALE = 1;
const MAX_SCALE = 5;

const fileInput = ref(null);
const imageUrl = ref("");
const image = ref(null); // HTMLImageElement
const naturalWidth = ref(0);
const naturalHeight = ref(0);
const scaleFactor = ref(1);
const offsetX = ref(0);
const offsetY = ref(0);
const dragging = ref(false);
const uploading = ref(false);
const errorMsg = ref("");

// 初始缩放：让图片较短的一边刚好铺满视口，保证任意位置都在图片范围内
const baseScale = computed(() => {
  if (!naturalWidth.value || !naturalHeight.value) return 1;
  return VIEWPORT / Math.min(naturalWidth.value, naturalHeight.value);
});

const displayWidth = computed(
  () => naturalWidth.value * baseScale.value * scaleFactor.value,
);
const displayHeight = computed(
  () => naturalHeight.value * baseScale.value * scaleFactor.value,
);

const imageStyle = computed(() => ({
  width: displayWidth.value + "px",
  height: displayHeight.value + "px",
  transform: `translate(${offsetX.value}px, ${offsetY.value}px)`,
}));

function clampOffsets() {
  offsetX.value = Math.min(
    0,
    Math.max(VIEWPORT - displayWidth.value, offsetX.value),
  );
  offsetY.value = Math.min(
    0,
    Math.max(VIEWPORT - displayHeight.value, offsetY.value),
  );
}

watch([displayWidth, displayHeight], clampOffsets);

// 关闭弹窗时重置状态，避免下次打开残留旧图片
watch(
  () => props.visible,
  (visible) => {
    if (!visible) reset();
  },
);

function reset() {
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value);
  imageUrl.value = "";
  image.value = null;
  naturalWidth.value = 0;
  naturalHeight.value = 0;
  scaleFactor.value = 1;
  offsetX.value = 0;
  offsetY.value = 0;
  errorMsg.value = "";
}

function openFilePicker() {
  fileInput.value?.click();
}

function onFileChange(event) {
  const file = event.target.files?.[0];
  event.target.value = "";
  if (!file) return;
  if (!file.type.startsWith("image/")) {
    errorMsg.value = "请选择图片文件";
    return;
  }
  errorMsg.value = "";
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value);
  imageUrl.value = URL.createObjectURL(file);

  const img = new Image();
  img.onload = () => {
    image.value = img;
    naturalWidth.value = img.naturalWidth;
    naturalHeight.value = img.naturalHeight;
    scaleFactor.value = 1;
    offsetX.value = (VIEWPORT - displayWidth.value) / 2;
    offsetY.value = (VIEWPORT - displayHeight.value) / 2;
  };
  img.src = imageUrl.value;
}

// 拖拽移动
let startX = 0;
let startY = 0;
let startOffsetX = 0;
let startOffsetY = 0;

function onPointerDown(event) {
  dragging.value = true;
  startX = event.clientX;
  startY = event.clientY;
  startOffsetX = offsetX.value;
  startOffsetY = offsetY.value;
  event.currentTarget.setPointerCapture(event.pointerId);
}

function onPointerMove(event) {
  if (!dragging.value) return;
  offsetX.value = startOffsetX + (event.clientX - startX);
  offsetY.value = startOffsetY + (event.clientY - startY);
  clampOffsets();
}

function onPointerUp() {
  dragging.value = false;
}

// 以视口中心为基准缩放，缩放时尽量保持画面中心不跑
function setScale(next) {
  const prev = scaleFactor.value;
  next = Math.min(MAX_SCALE, Math.max(MIN_SCALE, next));
  if (next === prev) return;
  const ratio = next / prev;
  const center = VIEWPORT / 2;
  offsetX.value = center - (center - offsetX.value) * ratio;
  offsetY.value = center - (center - offsetY.value) * ratio;
  scaleFactor.value = next;
  clampOffsets();
}

function onWheel(event) {
  setScale(scaleFactor.value * (event.deltaY < 0 ? 1.1 : 0.9));
}

// 把视口内可见区域按比例画到 canvas 上，裁剪出正方形头像
function cropToDataUrl() {
  const canvas = document.createElement("canvas");
  canvas.width = OUTPUT_SIZE;
  canvas.height = OUTPUT_SIZE;
  const ctx = canvas.getContext("2d");

  const totalScale = baseScale.value * scaleFactor.value;
  const sx = -offsetX.value / totalScale;
  const sy = -offsetY.value / totalScale;
  const sSize = VIEWPORT / totalScale;

  // JPEG 输出：体积远小于 PNG（108KB 的照片转 PNG 可能膨胀到 1MB+），头像不需要透明通道
  ctx.drawImage(
    image.value,
    sx,
    sy,
    sSize,
    sSize,
    0,
    0,
    OUTPUT_SIZE,
    OUTPUT_SIZE,
  );
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error("头像处理失败"))),
      "image/jpeg",
      0.92,
    );
  });
}

async function onConfirm() {
  if (!image.value || uploading.value) return;
  uploading.value = true;
  errorMsg.value = "";
  try {
    const imageBlob = await cropToDataUrl();
    const result = await updateUserAvatar(imageBlob);
    emit("uploaded", result?.avatar || "");
    onClose();
  } catch (err) {
    errorMsg.value = err.message || "头像上传失败，请稍后重试";
  } finally {
    uploading.value = false;
  }
}

function onClose() {
  emit("close");
}
</script>

<style scoped src="@/style/avatarUploader.less"></style>
