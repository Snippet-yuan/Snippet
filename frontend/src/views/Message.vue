<template>
  <div>
    <MessageLayout />
  </div>
</template>

<script setup>
import { ref } from "vue";
import MessageLayout from "@/layouts/MessageLayout.vue";
const msg = ref("");
</script>

<style scoped></style>
