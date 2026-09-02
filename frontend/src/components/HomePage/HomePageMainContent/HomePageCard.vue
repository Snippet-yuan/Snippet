<template>
  <div class="card-container">
    <div class="card-item" v-for="post in posts" :key="post.id">
      <div class="card-item-info">
        <MediaPreview :images="post.images" />

        <CardInfo
          v-model:counters="post.counters"
          v-model:favorited="post.favorited"
          v-model:liked="post.liked"
          :owner-avatar="post.ownerAvatar"
          :owner-nickname="post.ownerNickname"
        />
      </div>

      <div class="card-title-container">
        <h2 class="card-title">
          {{ post.title }}
        </h2>
      </div>

      <!-- 可折叠描述 -->
      <div class="card-description-container" @click="toggleDescription">
        <p class="card-description" :class="{ collapsed: !isExpanded }">
          {{ post.description }}
        </p>

        <PhCaretDown
          class="expand-icon"
          :class="{ rotated: isExpanded }"
          :size="18"
        />
      </div>

      <!-- 发布时间：始终在卡片最底部右下角 -->
      <div class="card-footer">
        <span class="publish-time">
          {{ formatTime(post.createdAt, "MM-DD HH:mm") }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import CardInfo from "./CardInfo.vue";
import MediaPreview from "./MediaPreview.vue";
import { PhCaretDown } from "@phosphor-icons/vue";

//--------------------------------------------------------------------------------
//获取帖子的长列表

import getPosts from "@/api/getPost";
import { formatTime } from "@/utils/timeFormat";

const posts = ref([]);

onMounted(async () => {
  const res = await getPosts();

  posts.value = res.data.items;
});

//--------------------------------------------------------------------------------

const isExpanded = ref(false);

const toggleDescription = () => {
  isExpanded.value = !isExpanded.value;
};
</script>

<style scoped src="@/style/homePageCard.less"></style>
