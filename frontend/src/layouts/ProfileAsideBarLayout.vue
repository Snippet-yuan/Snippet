<template>
  <div class="profile-aside-bar">
    <FriendList />
    <Follows />
    <YourFollowers />
  </div>
</template>

<script setup>
import FriendList from "@/components/Profile/AsideBar/FriendList.vue";
import Follows from "@/components/Profile/AsideBar/Follows.vue";
import YourFollowers from "@/components/Profile/AsideBar/YourFollowers.vue";
</script>

<style scoped>
.profile-aside-bar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
