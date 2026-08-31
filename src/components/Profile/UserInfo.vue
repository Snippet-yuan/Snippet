<template>
  <div class="user-background">
    <img class="user-background-image" src="@/assets/images/bg-2.jpg" alt="" />
  </div>
  <div class="user-avatar">
    <img
      class="user-avatar-image"
      src="@/assets/avatar/user-avatar.jpeg"
      alt=""
    />
  </div>
  <button class="edit-profile-button">编辑资料</button>
  <div class="user-detail-info">
    <div class="user-name">周易</div>
    <div class="user-id">
      <span class="user-id-prefix">ID:</span>
      <span class="user-id-number">1234567890</span>
    </div>
    <div class="user-signature">成为一个安静的人，需要付出很多。</div>
  </div>
  <div class="join-date">
    <span class="join-date-prefix">加入时间:</span>
    <span class="join-date-number">2026-01-01</span>
  </div>

  <div>
    <img class="snippet-svg" src="/Snippet-text.svg" alt="" />
  </div>
</template>

<script setup>
import { ref } from "vue";

const msg = ref("");
</script>

<style scoped src="@/style/userInfo.less"></style>
