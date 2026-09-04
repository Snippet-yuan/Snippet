<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="handleClose">
      <div class="modal-box">
        <!-- 关闭按钮 -->
        <button class="close-btn" @click="handleClose" aria-label="关闭">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path
              fill="currentColor"
              d="M10.59 12L4.54 5.96l1.42-1.42L12 10.59l6.04-6.05 1.42 1.42L13.41 12l6.05 6.04-1.42 1.42L12 13.41l-6.04 6.05-1.42-1.42L10.59 12z"
            />
          </svg>
        </button>

        <div class="profile">
          <!-- 顶部横幅 -->
          <div class="banner-wrap">
            <img
              v-if="ownerInfo.background"
              class="banner"
              :src="ownerInfo.background"
              alt="背景"
            />
            <div v-else class="banner banner--placeholder"></div>
          </div>

          <!-- 头像 + 操作按钮行 -->
          <div class="avatar-actions">
            <div class="avatar-wrap">
              <img
                v-if="ownerInfo.avatar"
                class="avatar"
                :src="ownerInfo.avatar"
                alt="头像"
              />
              <div v-else class="avatar avatar--placeholder"></div>
            </div>

            <div class="actions">
              <button class="btn btn--outline">添加好友</button>
              <button class="btn btn--primary">关注</button>
            </div>
          </div>

          <!-- 用户信息 -->
          <div class="user-info">
            <div class="name-row">
              <h1 class="name">{{ ownerInfo.nickname || "加载中..." }}</h1>
            </div>

            <div class="handle">
              <span class="id-tag">ID</span>
              <span class="id">{{ ownerInfo.id }}</span>
            </div>

            <p v-if="ownerInfo.bio" class="bio">{{ ownerInfo.bio }}</p>

            <div class="meta">
              <span v-if="ownerInfo.createdAt" class="meta-item">
                <svg
                  viewBox="0 0 24 24"
                  width="18"
                  height="18"
                  class="meta-icon"
                >
                  <path
                    fill="currentColor"
                    d="M7 4V3h2v1h6V3h2v1h1.5C19.89 4 21 5.12 21 6.5v12c0 1.38-1.11 2.5-2.5 2.5h-13C4.12 21 3 19.88 3 18.5v-12C3 5.12 4.12 4 5.5 4H7zm0 2H5.5c-.28 0-.5.22-.5.5v12c0 .28.22.5.5.5h13c.28 0 .5-.22.5-.5v-12c0-.28-.22-.5-.5-.5H17v1h-2V6H9v1H7V6zm0 4h10v2H7v-2zm0 4h7v2H7v-2z"
                  />
                </svg>
                {{ formatTime(ownerInfo.createdAt, "YYYY-MM-DD") }}
              </span>
            </div>

            <!-- 关注数据（有字段再显示） -->
            <div
              v-if="ownerInfo.following != null || ownerInfo.followers != null"
              class="stats"
            >
              <span class="stat">
                <strong>{{ formatCount(ownerInfo.following) }}</strong>
                正在关注
              </span>
              <span class="stat">
                <strong>{{ formatCount(ownerInfo.followers) }}</strong>
                关注者
              </span>
            </div>
          </div>
        </div>
        <div class="tag-item-container">
          <TagItem v-for="post in postList" :post="post" :key="post.id" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { watch, onBeforeUnmount, ref, onMounted } from "vue";
import { getPostOwnerInfo } from "@/api/getPostOwnerInfo";
import { formatTime } from "@/utils/timeFormat";
import TagItem from "@/components/Profile/TagItem.vue";
import { getOthersPost } from "@/api/getOthersPost";

const props = defineProps({
  visible: Boolean,
  userId: String,
});

const emit = defineEmits(["close"]);

function handleClose() {
  emit("close");
}

const BODY_CLASS = "post-owner-info-open";

function syncBodyState(visible) {
  document.body.classList.toggle(BODY_CLASS, visible);
  document.body.style.overflow = visible ? "hidden" : "";
}

const ownerInfo = ref({});

async function loadOwnerInfo() {
  if (!props.userId) return;
  try {
    ownerInfo.value = await getPostOwnerInfo(props.userId);
  } catch (error) {
    console.error(error);
  }
}

function formatCount(n) {
  if (n == null) return "0";
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, "") + "万";
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, "") + "K";
  return String(n);
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) loadOwnerInfo();
  },
  { immediate: true },
);

watch(() => props.visible, syncBodyState);

onBeforeUnmount(() => {
  syncBodyState(false);
});

//获取该用户发布的帖子
const page = ref(1);
const limit = ref(20);
const postList = ref([]);
onMounted(async () => {
  const posts = await getOthersPost(props.userId, page.value, limit.value);
  postList.value = [...postList.value, ...posts.items];
});
</script>

<style>
/* 打开时把导航抬到遮罩之上 */
body.post-owner-info-open .nav-bar,
body.post-owner-info-open .search-bar {
  z-index: 1100;
}
</style>

<style scoped src="@/style/postOwnerInfo.css"></style>
