<template>
  <section class="aside-card friend-list">
    <div class="aside-card-header">
      <div>
        <p class="aside-card-kicker">Connections</p>
        <h3 class="aside-card-title">你的朋友</h3>
      </div>
      <span class="aside-card-badge">8</span>
    </div>

    <div class="avatar-stack" aria-label="好友头像列表">
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar.jpeg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-2.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-3.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-4.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-5.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-6.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-7.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item">
        <img
          class="avatar-image"
          src="@/assets/avatar/user-avatar-8.jpg"
          alt="好友头像"
        />
      </div>
      <div class="avatar-item-add">
        <PhUserCirclePlus class="avatar-item-add-icon" :size="32" />
      </div>
    </div>

    <div class="friend-summary">
      <div class="summary-row">
        <span class="summary-label">最近在线</span>
        <span class="summary-value">3 人</span>
      </div>
      <div class="summary-row">
        <span class="summary-label">本周新增</span>
        <span class="summary-value">2 人</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { PhUserCirclePlus } from "@phosphor-icons/vue";
</script>

<style scoped src="@/style/friendList.less"></style>
