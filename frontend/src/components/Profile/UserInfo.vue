<template>
  <div class="user-background">
    <img class="user-background-image" :src="userInfo.background" alt="" />
    <PhImage class="user-background-image-icon" :size="32" />
  </div>
  <div class="user-avatar">
    <img class="user-avatar-image" :src="userInfo.avatar" alt="" />
    <div class="user-avatar-icon-container" @click="showAvatarUploader = true">
      <PhCamera class="user-avatar-image-icon" :size="32" />
    </div>
  </div>
  <AvatarUploader
    :visible="showAvatarUploader"
    @close="showAvatarUploader = false"
    @uploaded="onAvatarUploaded"
  />
  <button class="edit-profile-button">编辑资料</button>
  <div class="user-detail-info">
    <div class="user-name">{{ userInfo.nickname }}</div>
    <div class="user-id">
      <span class="user-id-prefix">ID:</span>
      <span class="user-id-number">{{ userInfo.id }}</span>
    </div>
    <div class="user-signature">{{ userInfo.bio }}</div>
  </div>
  <div class="join-date">
    <span class="join-date-prefix">加入时间:</span>
    <span class="join-date-number">{{ userInfo.createdAt }}</span>
  </div>

  <div>
    <img class="snippet-svg" src="/Snippet-text.svg" alt="" />
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import { PhImage, PhCamera } from "@phosphor-icons/vue";
import { useUserInfoStore } from "@/stores/userInfo";
import { useUserStore } from "@/stores/user";
import AvatarUploader from "./AvatarUploader.vue";

const userInfoStore = useUserInfoStore();
const userStore = useUserStore();
// storeToRefs 保持响应式：store 更新后这个引用会跟着变
const { userInfo } = storeToRefs(userInfoStore);

const showAvatarUploader = ref(false);

// 头像上传成功后同步两个 store，导航栏和个人主页会立即显示新头像
function onAvatarUploaded(avatar) {
  userStore.updateProfile({ avatar });
  userInfo.value = { ...userInfo.value, avatar };
}

onMounted(async () => {
  // force=true：个人主页要完整资料，必须强制请求，不走缓存快照
  await userInfoStore.loadUserInfo(true);
});
</script>

<style scoped src="@/style/userInfo.less"></style>
