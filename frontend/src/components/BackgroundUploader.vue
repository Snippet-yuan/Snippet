<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="background-uploader-overlay"
      @click.self="onClose"
      @keydown.esc="onClose"
    >
      <div class="background-uploader-modal">
        <header class="background-uploader-header">
          <h3>更换背景图片</h3>
          <button
            class="background-uploader-close"
            type="button"
            @click="onClose"
          >
            <PhX :size="20" />
          </button>
        </header>

        <div
          v-if="!image"
          class="background-uploader-picker"
          @click="openFilePicker"
        >
          <PhImage :size="48" />
          <p>点击选择本地图片</p>
          <span>支持 JPG / PNG / WebP，最大 10MB</span>
        </div>

        <template v-else>
          <div
            class="background-uploader-viewport"
            :style="{
              width: VIEWPORT_WIDTH + 'px',
              height: VIEWPORT_HEIGHT + 'px',
            }"
            @pointerdown="onPointerDown"
            @pointermove="onPointerMove"
            @pointerup="onPointerUp"
            @pointercancel="onPointerUp"
            @wheel.prevent="onWheel"
          >
            <img
              :src="imageUrl"
              class="background-uploader-image"
              :style="imageStyle"
              draggable="false"
              alt="背景预览"
            />
            <div class="background-uploader-guide">背景预览区域</div>
          </div>

          <div class="background-uploader-zoom">
            <button type="button" @click="setScale(scaleFactor - 0.2)">
              <PhMinus :size="16" />
            </button>
            <input
              type="range"
              :min="MIN_SCALE"
              :max="MAX_SCALE"
              step="0.01"
              :value="scaleFactor"
              @input="setScale(Number($event.target.value))"
            />
            <button type="button" @click="setScale(scaleFactor + 0.2)">
              <PhPlus :size="16" />
            </button>
          </div>
          <p class="background-uploader-tip">
            拖拽移动图片，滚动鼠标或拖动滑块调整大小
          </p>
        </template>

        <p v-if="errorMsg" class="background-uploader-error">{{ errorMsg }}</p>

        <footer class="background-uploader-footer">
          <button
            v-if="image"
            class="background-uploader-btn background-uploader-btn-secondary"
            type="button"
            @click="openFilePicker"
          >
            重新选择
          </button>
          <button
            class="background-uploader-btn background-uploader-btn-secondary"
            type="button"
            @click="onClose"
          >
            取消
          </button>
          <button
            class="background-uploader-btn background-uploader-btn-primary"
            type="button"
            :disabled="!image || uploading"
            @click="onConfirm"
          >
            {{ uploading ? "上传中..." : "确定" }}
          </button>
        </footer>

        <input
          ref="fileInput"
          class="background-uploader-file-input"
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          @change="onFileChange"
        />
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { PhImage, PhMinus, PhPlus, PhX } from "@phosphor-icons/vue";
import { updateUserBackground } from "@/api/getUserInfo";

const props = defineProps({ visible: Boolean });
const emit = defineEmits(["close", "uploaded"]);
const VIEWPORT_WIDTH = 720;
const VIEWPORT_HEIGHT = 360;
const OUTPUT_WIDTH = 1440;
const OUTPUT_HEIGHT = 720;
const MIN_SCALE = 1;
const MAX_SCALE = 4;
const fileInput = ref(null);
const imageUrl = ref("");
const image = ref(null);
const naturalWidth = ref(0);
const naturalHeight = ref(0);
const scaleFactor = ref(1);
const offsetX = ref(0);
const offsetY = ref(0);
const uploading = ref(false);
const errorMsg = ref("");

const baseScale = computed(() => {
  if (!naturalWidth.value || !naturalHeight.value) return 1;
  return Math.max(
    VIEWPORT_WIDTH / naturalWidth.value,
    VIEWPORT_HEIGHT / naturalHeight.value,
  );
});
const displayWidth = computed(
  () => naturalWidth.value * baseScale.value * scaleFactor.value,
);
const displayHeight = computed(
  () => naturalHeight.value * baseScale.value * scaleFactor.value,
);
const imageStyle = computed(() => ({
  width: `${displayWidth.value}px`,
  height: `${displayHeight.value}px`,
  transform: `translate(${offsetX.value}px, ${offsetY.value}px)`,
}));

function clampOffsets() {
  offsetX.value = Math.min(
    0,
    Math.max(VIEWPORT_WIDTH - displayWidth.value, offsetX.value),
  );
  offsetY.value = Math.min(
    0,
    Math.max(VIEWPORT_HEIGHT - displayHeight.value, offsetY.value),
  );
}
watch([displayWidth, displayHeight], clampOffsets);
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
  if (file.size > 10 * 1024 * 1024) {
    errorMsg.value = "背景图片不能超过 10MB";
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
    offsetX.value = (VIEWPORT_WIDTH - displayWidth.value) / 2;
    offsetY.value = (VIEWPORT_HEIGHT - displayHeight.value) / 2;
  };
  img.onerror = () => {
    errorMsg.value = "图片读取失败，请重新选择";
  };
  img.src = imageUrl.value;
}

let startX = 0;
let startY = 0;
let startOffsetX = 0;
let startOffsetY = 0;
function onPointerDown(event) {
  startX = event.clientX;
  startY = event.clientY;
  startOffsetX = offsetX.value;
  startOffsetY = offsetY.value;
  event.currentTarget.setPointerCapture(event.pointerId);
}
function onPointerMove(event) {
  if (!event.currentTarget.hasPointerCapture(event.pointerId)) return;
  offsetX.value = startOffsetX + event.clientX - startX;
  offsetY.value = startOffsetY + event.clientY - startY;
  clampOffsets();
}
function onPointerUp(event) {
  if (event.currentTarget.hasPointerCapture(event.pointerId)) {
    event.currentTarget.releasePointerCapture(event.pointerId);
  }
}
function setScale(next) {
  const previous = scaleFactor.value;
  next = Math.min(MAX_SCALE, Math.max(MIN_SCALE, next));
  if (next === previous) return;
  const ratio = next / previous;
  offsetX.value =
    VIEWPORT_WIDTH / 2 - (VIEWPORT_WIDTH / 2 - offsetX.value) * ratio;
  offsetY.value =
    VIEWPORT_HEIGHT / 2 - (VIEWPORT_HEIGHT / 2 - offsetY.value) * ratio;
  scaleFactor.value = next;
  clampOffsets();
}
function onWheel(event) {
  setScale(scaleFactor.value * (event.deltaY < 0 ? 1.1 : 0.9));
}
function cropToBlob() {
  const canvas = document.createElement("canvas");
  canvas.width = OUTPUT_WIDTH;
  canvas.height = OUTPUT_HEIGHT;
  const context = canvas.getContext("2d");
  const totalScale = baseScale.value * scaleFactor.value;
  context.drawImage(
    image.value,
    -offsetX.value / totalScale,
    -offsetY.value / totalScale,
    VIEWPORT_WIDTH / totalScale,
    VIEWPORT_HEIGHT / totalScale,
    0,
    0,
    OUTPUT_WIDTH,
    OUTPUT_HEIGHT,
  );
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error("背景图片处理失败"))),
      "image/jpeg",
      0.9,
    );
  });
}
async function onConfirm() {
  if (!image.value || uploading.value) return;
  uploading.value = true;
  errorMsg.value = "";
  try {
    const result = await updateUserBackground(await cropToBlob());
    emit("uploaded", result?.background || "");
    onClose();
  } catch (error) {
    errorMsg.value = error.message || "背景图上传失败，请稍后重试";
  } finally {
    uploading.value = false;
  }
}
function onClose() {
  emit("close");
}
</script>

<style scoped>
.background-uploader-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.55);
}
.background-uploader-modal {
  width: min(780px, calc(100vw - 32px));
  padding: 22px 26px 26px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 16px 50px rgba(0, 0, 0, 0.24);
}
.background-uploader-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.background-uploader-header h3 {
  margin: 0;
  color: #1e1e2e;
  font-size: 19px;
}
.background-uploader-close,
.background-uploader-zoom button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dddde5;
  background: #fff;
  color: #1e1e2e;
  cursor: pointer;
}
.background-uploader-close {
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 50%;
}
.background-uploader-close:hover,
.background-uploader-zoom button:hover {
  background: #f1f1f5;
}
.background-uploader-picker {
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 2px dashed #c8c8d0;
  border-radius: 13px;
  color: #777788;
  cursor: pointer;
}
.background-uploader-picker:hover {
  border-color: #1e1e2e;
  color: #1e1e2e;
}
.background-uploader-picker p,
.background-uploader-picker span,
.background-uploader-tip {
  margin: 0;
}
.background-uploader-picker p {
  font-weight: 600;
}
.background-uploader-picker span,
.background-uploader-tip {
  font-size: 12px;
  color: #777788;
}
.background-uploader-viewport {
  position: relative;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 12px;
  background: #20202e;
  cursor: grab;
  touch-action: none;
}
.background-uploader-viewport:active {
  cursor: grabbing;
}
.background-uploader-image {
  position: absolute;
  top: 0;
  left: 0;
  max-width: none;
  user-select: none;
  -webkit-user-drag: none;
}
.background-uploader-guide {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}
.background-uploader-zoom {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}
.background-uploader-zoom button {
  width: 29px;
  height: 29px;
  border-radius: 50%;
}
.background-uploader-zoom input {
  flex: 1;
  accent-color: #1e1e2e;
}
.background-uploader-tip {
  margin-top: 8px;
  text-align: center;
}
.background-uploader-error {
  margin: 12px 0 0;
  color: #e5484d;
  font-size: 13px;
  text-align: center;
}
.background-uploader-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.background-uploader-btn {
  min-width: 88px;
  height: 36px;
  padding: 0 16px;
  border-radius: 18px;
  font-weight: 600;
  cursor: pointer;
}
.background-uploader-btn-primary {
  border: 0;
  background: #1e1e2e;
  color: #fff;
}
.background-uploader-btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.background-uploader-btn-secondary {
  border: 1px solid #d8d8e0;
  background: #fff;
  color: #1e1e2e;
}
.background-uploader-file-input {
  display: none;
}
@media (max-width: 760px) {
  .background-uploader-modal {
    padding: 18px;
  }
  .background-uploader-viewport {
    width: 100% !important;
    height: auto !important;
    aspect-ratio: 2 / 1;
  }
  .background-uploader-picker {
    height: 240px;
  }
}
</style>
