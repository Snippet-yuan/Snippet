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

<style scoped>
.avatar-item-add {
  width: 46px;
  height: 46px;
  margin-left: -14px;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  /* border: 3px solid #fff; */
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.14);
  background: #d3d3d3;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    background-color 0.22s ease;
}

.avatar-item-add:hover {
  z-index: 20;
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 8px 18px rgba(31, 35, 41, 0.2);
  background: #c4c4c4;
}

.avatar-item-add-icon {
  width: 40px;
  height: 40px;
  color: #434343;
}

.aside-card {
  width: 420px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10px 30px rgba(31, 35, 41, 0.08);
  border: 1px solid rgba(91, 124, 250, 0.08);
}

.aside-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.aside-card-kicker {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #8a919f;
}

.aside-card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
}

.aside-card-badge {
  min-width: 34px;
  height: 34px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: linear-gradient(135deg, #5b7cfa 0%, #7f8cff 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(91, 124, 250, 0.28);
}

.avatar-stack {
  display: flex;
  align-items: center;
  padding: 8px 0 16px;
}

.avatar-item {
  margin-left: -14px;
  position: relative;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease;
}

.avatar-item:first-child {
  margin-left: 0;
}

/* Keep earlier avatars above later ones in the overlapping stack. */
.avatar-item:nth-child(1) {
  z-index: 9;
}

.avatar-item:nth-child(2) {
  z-index: 8;
}

.avatar-item:nth-child(3) {
  z-index: 7;
}

.avatar-item:nth-child(4) {
  z-index: 6;
}

.avatar-item:nth-child(5) {
  z-index: 5;
}

.avatar-item:nth-child(6) {
  z-index: 4;
}

.avatar-item:nth-child(7) {
  z-index: 3;
}

.avatar-item:nth-child(8) {
  z-index: 2;
}

.avatar-item:hover {
  z-index: 20;
  transform: translateY(-4px) scale(1.08);
}

.avatar-image {
  width: 46px;
  height: 46px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.14);
  background: #eef2f8;
}

.friend-summary {
  display: grid;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid #edf1f7;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.summary-label {
  color: #6b7280;
}

.summary-value {
  color: #1f2329;
  font-weight: 600;
}
</style>
