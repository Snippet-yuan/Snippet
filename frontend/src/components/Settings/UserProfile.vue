<template>
  <section class="settings-card">
    <div class="card-header">
      <h2>个人资料</h2>
      <p>编辑你的公开资料信息</p>
    </div>

    <div class="profile-preview">
      <div class="avatar-wrapper">
        <img :src="userInfo.avatar" alt="avatar" />
        <button
          class="avatar-edit"
          type="button"
          @click="showAvatarUploader = true"
        >
          <PhCamera :size="16" />
        </button>
        <AvatarUploader
          :visible="showAvatarUploader"
          @close="showAvatarUploader = false"
          @uploaded="onAvatarUploaded"
        />
      </div>
      <div class="profile-names">
        <h3>{{ before }}</h3>
        <span>{{ after }}</span>
      </div>
    </div>

    <div class="form-row">
      <label>显示名称</label>
      <input v-model="form.name" type="text" placeholder="你的名称" />
    </div>

    <div class="form-row">
      <label>个人简介</label>
      <textarea
        v-model="form.bio"
        rows="3"
        placeholder="写点什么介绍自己..."
      ></textarea>
    </div>

    <div class="form-row">
      <label>社交链接</label>
      <div class="social-stack">
        <div v-for="i in 3" :key="i" class="social-input">
          <PhLink :size="18" />
          <input
            v-model="form['social' + i]"
            type="text"
            placeholder="https://"
          />
        </div>
      </div>
    </div>

    <div class="card-footer">
      <button class="btn-primary" type="button" @click="saveChanges">
        保存更改
      </button>
    </div>

    <Toast ref="toastRef" />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { PhLink, PhCamera } from "@phosphor-icons/vue";
import { useUserInfoStore } from "@/stores/userInfo";
import { useUserStore } from "@/stores/user";
import { storeToRefs } from "pinia";
import AvatarUploader from "@/components/Profile/AvatarUploader.vue";
import Toast from "@/components/Toast.vue";

const showAvatarUploader = ref(false);
const toastRef = ref(null);

const userStore = useUserStore();

// 头像上传成功后同步两个 store，导航栏和个人主页会立即显示新头像
function onAvatarUploaded(avatar) {
  userStore.updateProfile({ avatar });
  userInfo.value = { ...userInfo.value, avatar };
}

const userInfoStore = useUserInfoStore();
const { userInfo } = storeToRefs(userInfoStore);

const form = reactive({
  name: "",
  bio: "",
  social1: "",
  social2: "",
  social3: "",
});

onMounted(async () => {
  await userInfoStore.loadUserInfo(true);

  form.name = userInfo.value.nickname;
  form.bio = userInfo.value.bio;

  console.log(userInfo.value);
});

//=============================================================
// 正则表达式拆分邮箱为前缀和后缀
const emailParts = computed(() => {
  const email = userInfo.value.email || "";
  const match = email.match(/^([^@]+)@(.+)$/);

  return {
    before: match?.[1] || email,
    after: match?.[2] ? `@${match[2]}` : "",
  };
});
const before = computed(() => emailParts.value.before);
const after = computed(() => emailParts.value.after);

//=============================================================
//保存用户信息的更改
async function saveChanges() {
  try {
    await userInfoStore.updateProfile({
      nickname: form.name,
      bio: form.bio,
    });
    toastRef.value?.show({ type: "success", message: "保存成功" });
  } catch (error) {
    toastRef.value?.show({
      type: "error",
      message: error.message || "保存失败，请稍后重试",
    });
  }
}
</script>

<style scoped src="@/style/Settings/UserProfile.css"></style>
