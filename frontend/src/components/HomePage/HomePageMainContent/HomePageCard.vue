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

<style scoped src="@/style/homePageCard.less"></style>
