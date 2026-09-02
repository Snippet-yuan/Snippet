<template>
  <section class="aside-card friend-list">
    <div class="aside-card-header">
      <div>
        <p class="aside-card-kicker">Connections</p>
        <h3 class="aside-card-title">你的朋友</h3>
      </div>
      <span class="aside-card-badge">{{ friendsStore.friendlist.length }}</span>
    </div>

    <div class="avatar-stack" aria-label="好友头像列表">
      <div
        v-for="(friend, index) in friendsStore.friendlist"
        :key="friend.id"
        class="avatar-item"
        :style="{ '--stack-index': friendsStore.friendlist.length - index }"
      >
        <img class="avatar-image" :src="friend.avatar" :alt="friend.nickname" />
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
import { onMounted } from "vue";
import { PhUserCirclePlus } from "@phosphor-icons/vue";
import { useFriendsStore } from "@/stores/friends";

const friendsStore = useFriendsStore();

onMounted(async () => {
  await friendsStore.loadFriendsList();
});
</script>

<style scoped src="@/style/friendList.less"></style>
