<template>
  <Empty v-if="postList.length === 0" message="暂无内容" />
  <div v-else class="tag-item-container">
    <TagItem v-for="post in postList" :key="post.id" :post="post" />
  </div>
</template>

<script setup>
import TagItem from "../TagItem.vue";
import Empty from "@/components/Empty.vue";

import { useUserPostsStore } from "@/stores/userPosts";
import { onMounted } from "vue";
import { storeToRefs } from "pinia";

const userPostsStore = useUserPostsStore();
const { postList } = storeToRefs(userPostsStore);

onMounted(async () => {
  await userPostsStore.fetchUserPosts(1, 20);
});
</script>

<style scoped>
.tag-item-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  width: 600px;
  min-height: 422px;
  border: 1px solid black;
  border-right: none;
  border-bottom: none;
  /* padding-bottom: 91px; */
}

/* .tag-item-container :deep(.tag-item:nth-child(1)) {
  background-color: #fb3e3e;
} */
.tag-item-container :deep(.tag-item:nth-child(n + 2)) {
  background-color: #ffffff;
}
</style>
