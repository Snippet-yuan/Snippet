<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="user-info">
        <div class="user-info-item">
          <div class="profile-avatar">
            <img
              class="avatar-image"
              src="@/assets/avatar/user-avatar.jpeg"
              alt="avatar"
            />
          </div>
          <div class="profile-username">小秋</div>
          <div class="user-id">
            <div class="user-id-label">ID:</div>
            <div class="user-id-value">1234567890</div>
          </div>
        </div>
      </div>
    </div>

    <div class="friend-list">
      <!-- <div class="friend-list-item">
        <div class="friend-list-item-avatar">
          <img
            class="avatar-image"
            src="@/assets/avatar/user-avatar.jpeg"
            alt="avatar"
          />
        </div>
        <div class="friend-list-item-name">小秋</div>
      </div> -->
    </div>
  </div>
</template>

<script setup></script>

<style scoped src="@/style/profile.less"></style>
