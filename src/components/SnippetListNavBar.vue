<template>
  <div>
    <nav class="tag-list">
      <button
        class="tag"
        :class="{ 'tag-active': activeTag === 'Snippets' }"
        @click="handleToggleTag('Snippets')"
      >
        Snippets
      </button>

      <button
        class="tag"
        :class="{ 'tag-active': activeTag === 'Favorites' }"
        @click="handleToggleTag('Favorites')"
      >
        Favorites
      </button>

      <button
        class="tag"
        :class="{ 'tag-active': activeTag === 'Replies' }"
        @click="handleToggleTag('Replies')"
      >
        Replies
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref } from "vue";

const activeTag = ref("Snippets");

function handleToggleTag(tag) {
  activeTag.value = tag;
}
</script>

<style scoped src="@/style/SnippetListNavBar.less"></style>
