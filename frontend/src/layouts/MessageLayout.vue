<template>
  <div class="message-layout">
    <ConversationList />
    <Dialog />
  </div>
</template>

<script setup>
import { ref } from "vue";
import ConversationList from "@/components/Message/ConversationList.vue";
import Dialog from "@/components/Message/Dialog.vue";

const msg = ref("");
</script>

<style scoped>
.message-layout {
  /* margin-top: 72px; */
  display: flex;
  background-color: rgb(231, 231, 231);
}
</style>
