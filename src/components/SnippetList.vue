<template>
  <SnippetListNavBar />
  <main>
    <SnippetsTag />
    <!-- <FavoritesTag /> -->
    <RepliesTag />
  </main>
</template>

<script setup>
import { ref } from "vue";
import SnippetListNavBar from "./SnippetListNavBar.vue";
import FavoritesTag from "./Profile/FavoritesTag.vue";
import RepliesTag from "./Profile/RepliesTag.vue";
import SnippetsTag from "./Profile/SnippetsTag.vue";

const msg = ref("");
</script>

<style scoped>
main {
  background-color: #fff;
  width: 100px;
  height: auto;
}
</style>
