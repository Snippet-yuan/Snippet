<template>
  <div class="search-bar" :class="{ 'message-search-bar': isMessagePage }">
    <div class="search-box">
      <PhMagnifyingGlass class="search-icon" :size="24" />
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索"
        @keyup.enter="handleSearch"
      />
      <PhEraser class="search-earser" :size="24" />
      <PhCaretDown class="search-arrow" :size="24" />
    </div>
    <RouterLink to="/user/1" class="search-avatar" title="我的主页">
      <img
        class="user-avator"
        src="@/assets/avatar/user-avatar.jpeg"
        alt="avatar"
      />
    </RouterLink>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { PhMagnifyingGlass, PhEraser, PhCaretDown } from "@phosphor-icons/vue";

const route = useRoute();
const keyword = ref("");
const isMessagePage = computed(() => route.path.startsWith("/message"));

function handleSearch() {
  // TODO: 跳转到搜索结果页
  console.log("搜索", keyword.value.trim());
}
</script>

<style scoped src="@/style/searchBar.less"></style>
