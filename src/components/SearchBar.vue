<template>
  <div class="search-bar">
    <div class="search-box">
      <svg
        class="search-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <circle cx="11" cy="11" r="8" />
        <line x1="21" y1="21" x2="16.65" y2="16.65" />
      </svg>
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索代码片段、标签、作者..."
        @keyup.enter="handleSearch"
      />
    </div>
    <RouterLink to="/user/1" class="search-avatar" title="我的主页">
      小
    </RouterLink>
  </div>
</template>

<script setup>
import { ref } from "vue";

const keyword = ref("");

function handleSearch() {
  // TODO: 跳转到搜索结果页
  console.log("搜索", keyword.value.trim());
}
</script>

<style scoped src="@/style/searchBar.css"></style>
