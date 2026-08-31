<template>
  <div class="side-action-bar">
    <!-- 用户头像 + 关注 -->
    <div class="action-item avatar-wrap">
      <div class="avatar-box">
        <img
          class="avatar-img"
          src="@/assets/avatar/user-avatar-6.jpg"
          alt="avatar"
        />
        <div class="follow-badge">
          <PhPlus weight="bold" :size="10" color="#fff" />
        </div>
      </div>
      <span class="action-label">John Doe</span>
    </div>

    <!-- 点赞 -->
    <div
      class="action-item"
      :class="{ liked: isLiked }"
      @click.stop="toggleLike"
    >
      <div class="icon-circle">
        <PhHeart weight="fill" :size="28" class="action-icon" />
      </div>
      <span class="action-count">2.1k</span>
    </div>

    <!-- 评论 -->
    <div
      class="action-item"
      :class="{ commented: isCommented }"
      @click.stop="toggleComment"
    >
      <div class="icon-circle">
        <PhChatCircleText weight="fill" :size="28" class="action-icon" />
      </div>
      <span class="action-count">342</span>
    </div>

    <!-- 收藏 -->
    <div
      class="action-item"
      :class="{ collected: isCollected }"
      @click.stop="toggleCollect"
    >
      <div class="icon-circle">
        <PhBookmarks weight="fill" :size="26" class="action-icon" />
      </div>
      <span class="action-count">213</span>
    </div>

    <!-- 分享 -->
    <div
      class="action-item"
      :class="{ shared: isShared }"
      @click.stop="toggleShare"
    >
      <div class="icon-circle">
        <PhShareFat weight="fill" :size="26" class="action-icon" />
      </div>
      <span class="action-count">Share</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import {
  PhPlus,
  PhHeart,
  PhChatCircleText,
  PhBookmarks,
  PhShareFat,
} from "@phosphor-icons/vue";

const isLiked = ref(false);
const isCommented = ref(false);
const isCollected = ref(false);
const isShared = ref(false);

// 独立切换方法，互不影响
const toggleLike = () => {
  isLiked.value = !isLiked.value;
};

const toggleComment = () => {
  isCommented.value = !isCommented.value;
};

const toggleCollect = () => {
  isCollected.value = !isCollected.value;
};

const toggleShare = () => {
  isShared.value = !isShared.value;
};
</script>

<style scoped src="@/style/cardInfo.less"></style>
