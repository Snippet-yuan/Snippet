<template>
  <SnippetListNavBar v-model:activeTag="activeTag" />
  <main>
    <SnippetsTag v-if="activeTag === 'Snippets'" />
    <FavoritesTag v-else-if="activeTag === 'Favorites'" />
    <RepliesTag v-else-if="activeTag === 'Replies'" />
  </main>
</template>

<script setup>
import { ref } from "vue";
import SnippetListNavBar from "@/components/Profile/SnippetListNavBar.vue";
import FavoritesTag from "@/components/Profile/SnippetListNavBar/FavoritesTag.vue";
import RepliesTag from "@/components/Profile/SnippetListNavBar/RepliesTag.vue";
import SnippetsTag from "@/components/Profile/SnippetListNavBar/SnippetsTag.vue";

const activeTag = ref("Snippets");
</script>

<style scoped>
main {
  background-color: #fff;
  width: 100%;
  height: auto;
}
</style>
