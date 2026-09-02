<template>
  <div class="media-preview">
    <div class="img-container">
      <Transition :name="direction === 1 ? 'slide-next' : 'slide-prev'">
        <img
          v-if="currentImage"
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
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { PhCaretLeft, PhCaretRight } from "@phosphor-icons/vue";

const props = defineProps({
  images: {
    type: Array,
    default: () => [],
  },
});

const currentIndex = ref(0);
const direction = ref(1);

const currentImage = computed(() => props.images[currentIndex.value]);

// 当 images 长度变化时重置索引，防止越界
watch(
  () => props.images.length,
  () => {
    if (currentIndex.value >= props.images.length) {
      currentIndex.value = 0;
    }
  }
);

const goToImage = (index) => {
  if (index === currentIndex.value) return;
  direction.value = index > currentIndex.value ? 1 : -1;
  currentIndex.value = index;
};

const prevImage = () => {
  if (props.images.length === 0) return;
  direction.value = -1;
  currentIndex.value =
    (currentIndex.value - 1 + props.images.length) % props.images.length;
};

const nextImage = () => {
  if (props.images.length === 0) return;
  direction.value = 1;
  currentIndex.value = (currentIndex.value + 1) % props.images.length;
};
</script>

<style scoped src="@/style/mediaPreview.less"></style>