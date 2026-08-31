<script setup>
defineProps({
  isScrolled: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <div class="operation-bar" :class="{ 'is-scrolled': isScrolled }">edit</div>
</template>

<style scoped>
.operation-bar {
  position: fixed;
  top: 0;
  left: 72px;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /* Keep the scrollbar gutter outside the glass surface so it remains visible. */
  width: 404px;
  height: 80px;
  margin-bottom: 10px;
  padding: 0 20px;
  box-sizing: border-box;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.62);
  /* box-shadow:
    0 8px 24px rgba(48, 60, 92, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7); */
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
  color: #273142;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.operation-bar.is-scrolled {
  background: rgba(255, 255, 255, 0.78);
}
</style>
