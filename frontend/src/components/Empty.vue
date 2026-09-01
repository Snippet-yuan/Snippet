<template>
  <div class="empty">
    <div class="empty-content">
      <div class="empty-icon">
        <PhTray :size="48" weight="light" />
      </div>
      <p class="empty-text">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { PhTray } from "@phosphor-icons/vue";

defineProps({
  message: {
    type: String,
    default: "暂无内容",
  },
});
</script>

<style scoped>
.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 437px;
  padding: 40px 24px;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-align: center;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  color: #ccc;
  background-color: #fafafa;
}

.empty-text {
  margin: 0;
  font-size: 14px;
  font-weight: 400;
  color: #999;
  letter-spacing: 0.3px;
  text-align: center;
}
</style>
