<template>
  <div class="card-container"></div>
</template>

<script setup>
import { ref } from "vue";

const msg = ref("");
</script>

<style scoped>
.card-container {
  width: 500px;
  height: 500px;
  background-color: red;
}
</style>
