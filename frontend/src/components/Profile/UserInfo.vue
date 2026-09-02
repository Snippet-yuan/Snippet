<template>
  <div class="user-background">
    <img class="user-background-image" :src="userInfo.background" alt="" />
    <PhImage
      class="user-background-image-icon"
      :size="32"
      @click="showBackgroundUploader = true"
    />
    <BackgroundUploader
      :visible="showBackgroundUploader"
      @close="showBackgroundUploader = false"
      @uploaded="onBackgroundUploaded"
    />
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
  <button class="edit-profile-button" @click="goToSettings">编辑资料</button>
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
    <span class="join-date-number">
      {{ formatTime(userInfo.createdAt, "YYYY-MM-DD") }}
    </span>
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
import BackgroundUploader from "../BackgroundUploader.vue";
import { useRouter } from "vue-router";
import { formatTime } from "@/utils/timeFormat";

const router = useRouter();
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

//背景图片上传
const showBackgroundUploader = ref(false);

function onBackgroundUploaded(background) {
  userStore.updateProfile({ background });
  userInfo.value = { ...userInfo.value, background };
}

//点击编辑资料跳转setting页面
function goToSettings() {
  router.push("/settings");
}

onMounted(async () => {
  // force=true：个人主页要完整资料，必须强制请求，不走缓存快照
  await userInfoStore.loadUserInfo(true);

  console.log(userInfo.value);
});
</script>

<style scoped src="@/style/userInfo.less"></style>
