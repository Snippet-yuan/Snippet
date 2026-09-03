<template>
  <div class="card-container">
    <div class="card-item" v-for="post in posts" :key="post.id">
      <div class="card-item-info">
        <MediaPreview :images="post.images" />

        <CardInfo
          :post-id="post.id"
          v-model:counters="post.counters"
          v-model:liked="post.liked"
          v-model:favorited="post.favorited"
          v-model:commented="post.commented"
          v-model:shared="post.shared"
          :owner-avatar="post.ownerAvatar"
          :owner-nickname="post.ownerNickname"
          @toggle-comment="onToggleComment(post)"
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

    <!-- 全屏评论弹层：左侧帖子图片，右侧评论列 -->
    <Teleport to="body">
      <Transition name="comment-overlay">
        <div
          v-if="activeCommentPost"
          class="comment-overlay"
          @click.self="closeComments"
        >
          <div class="comment-modal">
            <!-- 左侧：帖子主体（图片预览 + 标题描述） -->
            <div class="modal-media">
              <MediaPreview
                :images="activeCommentPost.images"
                :width="modalImageWidth"
              />
              <div class="modal-caption">
                <img
                  class="caption-avatar"
                  :src="activeCommentPost.ownerAvatar"
                  alt=""
                />
                <div class="caption-body">
                  <p class="caption-nickname">
                    {{ activeCommentPost.ownerNickname }}
                  </p>
                  <p v-if="activeCommentPost.title" class="caption-title">
                    {{ activeCommentPost.title }}
                  </p>
                  <p v-if="activeCommentPost.description" class="caption-desc">
                    {{ activeCommentPost.description }}
                  </p>
                </div>
              </div>
            </div>

            <!-- 右侧：评论列，从帖子图片下方/左侧滑出 -->
            <div class="modal-side">
              <Comment :post-id="activeCommentPost.id" @close="closeComments" />
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import CardInfo from "./CardInfo.vue";
import MediaPreview from "./MediaPreview.vue";
import Comment from "@/components/Comment.vue";
import { PhCaretDown } from "@phosphor-icons/vue";

//--------------------------------------------------------------------------------
//获取帖子的长列表

import getPosts from "@/api/getPost";
import { formatTime } from "@/utils/timeFormat";

const posts = ref([]);

onMounted(async () => {
  const res = await getPosts();

  // 后端每帖只返回 liked / favorited 两个状态，
  // 这里补齐 commented / shared 默认值，供四个操作统一 v-model 使用
  posts.value = res.data.items.map((post) => ({
    commented: false,
    shared: false,
    ...post,
  }));
});

//--------------------------------------------------------------------------------

const isExpanded = ref(false);

const toggleDescription = () => {
  isExpanded.value = !isExpanded.value;
};

//--------------------------------------------------------------------------------
// 全屏评论弹层

const activeCommentPost = ref(null);

// 弹层内图片宽度：窄屏适当缩小，避免溢出
const modalImageWidth = computed(() => (window.innerWidth < 900 ? 500 : 600));

const onToggleComment = (post) => {
  // 再次点击同一张帖子：关闭面板
  if (activeCommentPost.value?.id === post.id) {
    closeComments();
    return;
  }
  activeCommentPost.value = post;
};

const closeComments = () => {
  if (activeCommentPost.value) {
    activeCommentPost.value.commented = false;
  }
  activeCommentPost.value = null;
};

// 弹层打开时锁定页面滚动，关闭时恢复
watch(activeCommentPost, (post) => {
  document.body.style.overflow = post ? "hidden" : "";
});

// Esc 关闭弹层
const onKeydown = (e) => {
  if (e.key === "Escape") closeComments();
};

onMounted(() => window.addEventListener("keydown", onKeydown));
onUnmounted(() => {
  window.removeEventListener("keydown", onKeydown);
  document.body.style.overflow = "";
});
</script>

<style scoped src="@/style/homePageCard.less"></style>
