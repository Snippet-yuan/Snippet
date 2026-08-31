<template>
  <div class="operation-bar">edit</div>
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
  width: 414px;
  height: 80px;
  margin-bottom: 10px;
  padding: 0 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.62);
  box-shadow:
    0 8px 24px rgba(48, 60, 92, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
  color: #273142;
}
</style>
