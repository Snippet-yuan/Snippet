<template>
  <div class="tag-item">
    <img class="cover-image" :src="post.images[0]" alt="" />
    <div class="title">
      <h3>{{ truncateTitle(post.title) }}</h3>
    </div>
    <!-- <div class="comment-count">
      <span>{{ post.comments }} 评论</span>
    </div> -->
    <div class="user-info">
      <img class="user-avatar" :src="post.ownerAvatar" alt="" />
      <span class="user-name">{{ post.ownerNickname }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  post: {
    type: Object,
    required: true,
  },
});

// 标题超过 9 个字时省略为前 9 个字 + "..."
function truncateTitle(title, maxLength = 10) {
  const chars = Array.from(title || "");
  return chars.length > maxLength
    ? `${chars.slice(0, maxLength).join("")}...`
    : title || "";
}
</script>

<style scoped>
.tag-item {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  width: 200px;
  height: 300px;
  padding: 4px;
  border: 1px solid black;
  border-left: none;
  border-top: none;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.2s ease;

  position: relative;
}

.tag-item:hover {
  background-color: #f5f5f5;
}

.cover-image {
  width: 100%;
  height: 235px;
  object-fit: cover;
  border-radius: 8px;
  background-color: #e0e0e0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  position: absolute;
  bottom: 4px;
  left: 4px;
}

.user-name {
  font-size: 14px;
  color: #000;
  font-weight: 600;
  position: absolute;
  bottom: 4px;
  left: 46px;
}

.comment-count {
  position: absolute;
  bottom: 4px;
  right: 8px;
  font-size: 12px;
  color: #000;
  font-weight: 400;
}

.title {
  position: absolute;
  bottom: 34px;
  left: 4px;
  right: 4px;
  font-size: 14px;
  color: #000;
  font-weight: 600;
}

.title h3 {
  margin: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
