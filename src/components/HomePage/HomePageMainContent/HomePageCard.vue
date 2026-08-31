<template>
  <div class="card-container">
    <div class="card-item">
      <div class="card-item-info">
        <div class="media-preview">
          <div class="img-container">
            <Transition :name="direction === 1 ? 'slide-next' : 'slide-prev'">
              <img
                :key="currentIndex"
                class="img-item"
                :src="currentImage"
                alt=""
              />
            </Transition>
            <PhCaretLeft
              v-if="images.length > 1"
              class="nav-arrow nav-arrow-left"
              :size="28"
              @click="prevImage"
            />
            <PhCaretRight
              v-if="images.length > 1"
              class="nav-arrow nav-arrow-right"
              :size="28"
              @click="nextImage"
            />
          </div>
          <div v-if="images.length > 1" class="dots-container">
            <span
              v-for="(img, index) in images"
              :key="img"
              class="dot"
              :class="{ active: currentIndex === index }"
              @click="goToImage(index)"
            ></span>
          </div>
        </div>

        <CardInfo />
      </div>

      <div class="card-titl-container">
        <h2 class="card-titl">
          Lorem ipsum dolor sit amet consectetur adipisicing elit.
        </h2>
      </div>

      <!-- 可折叠描述 -->
      <div class="card-description-container" @click="toggleDescription">
        <p class="card-description" :class="{ collapsed: !isExpanded }">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quisquam,
          quos. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Quisquam, quos.Lorem ipsum dolor sit amet consectetur adipisicing
          elit. Quisquam, quos. Lorem ipsum dolor sit amet consectetur
          adipisicing elit. Quisquam, quos. Lorem ipsum dolor sit amet
          consectetur adipisicing elit. Quisquam, quos.Lorem ipsum dolor sit
          amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum dolor
          sit amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum
          dolor sit amet consectetur adipisicing elit. Quisquam, quos.Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Quisquam, quos.
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quisquam,
          quos. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Quisquam, quos.Lorem ipsum dolor sit amet consectetur adipisicing
          elit. Quisquam, quos. Lorem ipsum dolor sit amet consectetur
          adipisicing elit. Quisquam, quos. Lorem ipsum dolor sit amet
          consectetur adipisicing elit. Quisquam, quos.Lorem ipsum dolor sit
          amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum dolor
          sit amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum
          dolor sit amet consectetur adipisicing elit. Quisquam, quos.Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Quisquam, quos.
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quisquam,
          quos. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Quisquam, quos.Lorem ipsum dolor sit amet consectetur adipisicing
          elit. Quisquam, quos. Lorem ipsum dolor sit amet consectetur
          adipisicing elit. Quisquam, quos. Lorem ipsum dolor sit amet
          consectetur adipisicing elit. Quisquam, quos.Lorem ipsum dolor sit
          amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum dolor
          sit amet consectetur adipisicing elit. Quisquam, quos. Lorem ipsum
          dolor sit amet consectetur adipisicing elit. Quisquam, quos.Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Quisquam, quos.
        </p>

        <PhCaretDown
          class="expand-icon"
          :class="{ rotated: isExpanded }"
          :size="18"
        />
      </div>

      <!-- 发布时间：始终在卡片最底部右下角 -->
      <div class="card-footer">
        <span class="publish-time">2026-08-31 12:00</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import CardInfo from "./CardInfo.vue";
import {
  PhPencilSimpleLine,
  PhBookmarks,
  PhCaretDown,
  PhCaretLeft,
  PhCaretRight,
} from "@phosphor-icons/vue";

const images = ref([
  new URL("@/assets/images/bg-3.jpg", import.meta.url).href,
  new URL("@/assets/images/bg-4.jpg", import.meta.url).href,
  new URL("@/assets/images/bg-5.jpg", import.meta.url).href,
]);
const currentIndex = ref(0);
const currentImage = computed(() => images.value[currentIndex.value]);
const direction = ref(1);

const isExpanded = ref(false);

const toggleDescription = () => {
  isExpanded.value = !isExpanded.value;
};

const goToImage = (index) => {
  if (index === currentIndex.value) return;
  direction.value = index > currentIndex.value ? 1 : -1;
  currentIndex.value = index;
};

const prevImage = () => {
  direction.value = -1;
  currentIndex.value =
    (currentIndex.value - 1 + images.value.length) % images.value.length;
};

const nextImage = () => {
  direction.value = 1;
  currentIndex.value = (currentIndex.value + 1) % images.value.length;
};
</script>

<style scoped>
.card-container {
  background-color: rgb(255, 255, 255);
}

.card-item {
  display: flex;
  flex-direction: column;
  padding: 20px;
  width: 720px;
  height: auto;
  background-color: rgb(255, 255, 255);
}

.img-container {
  position: relative;
  display: inline-block;
  width: 600px;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background-color: #f0f0f0;
}

.img-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  object-position: center;
}

/* 左右切换动画 */
.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition: transform 0.4s ease;
}

.slide-next-enter-from {
  transform: translateX(100%);
}

.slide-next-leave-to {
  transform: translateX(-100%);
}

.slide-prev-enter-from {
  transform: translateX(-100%);
}

.slide-prev-leave-to {
  transform: translateX(100%);
}

.media-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.nav-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  padding: 8px;
  z-index: 1;
  transition: background-color 0.2s ease;
}

.nav-arrow:hover {
  background-color: rgba(0, 0, 0, 0.6);
}

.nav-arrow-left {
  left: 12px;
}

.nav-arrow-right {
  right: 12px;
}

.dots-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 10px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #d0d0d0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dot:hover {
  background-color: #999;
}

.dot.active {
  background-color: #333;
}

.card-item-info {
  display: flex;
  gap: 16px;
}

/* ========== 描述折叠样式 ========== */
.card-description-container {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  cursor: pointer;
  user-select: none;
}

.card-description {
  flex: 1;
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  transition: all 0.25s ease;
}

/* 折叠状态：只显示一行 */
.card-description.collapsed {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 箭头 */
.expand-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: #666;
  transition: transform 0.25s ease;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

/* ========== 发布时间（始终在卡片底部右下角） ========== */
.card-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.publish-time {
  font-size: 12px;
  color: #999;
  line-height: 1;
}
</style>
