<template>
  <section class="comment-panel">
    <header class="panel-header">
      <div class="panel-title">
        <PhChatCircleText class="panel-title-icon" :size="20" weight="fill" />
        <span>评论</span>
        <span class="panel-title-count">{{ commentList.length }}</span>
      </div>

      <button
        class="panel-close"
        type="button"
        aria-label="关闭评论"
        @click="emit('close')"
      >
        <PhX :size="20" weight="bold" />
      </button>
    </header>

    <div v-if="loading" class="panel-empty">正在加载评论…</div>

    <div v-else-if="errorMessage" class="panel-empty panel-error">
      {{ errorMessage }}
    </div>

    <div v-else-if="commentList.length === 0" class="panel-empty">
      <PhChatCircleDots class="panel-empty-icon" :size="40" weight="thin" />
      <p>还没有评论，来抢个沙发～</p>
    </div>

    <ul v-else class="comment-list">
      <li v-for="comment in commentList" :key="comment.id" class="comment-item">
        <img
          :src="comment.authorAvatar"
          :alt="comment.authorNickname"
          class="comment-avatar"
          loading="lazy"
          @error="handleAvatarError"
        />

        <div class="comment-body">
          <div class="comment-meta">
            <span class="comment-nickname">{{ comment.authorNickname }}</span>
            <span class="comment-time">
              {{ formatTime(comment.createdAt, "YYYY-MM-DD HH:mm") }}
            </span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
        </div>
      </li>
    </ul>

    <!-- 底部：发送评论 -->
    <footer class="panel-footer">
      <p v-if="sendError" class="send-error">{{ sendError }}</p>
      <div class="send-bar">
        <input
          v-model.trim="draft"
          class="comment-input"
          type="text"
          placeholder="友善评论，理性发言…"
          maxlength="200"
          :disabled="sending"
          @keyup.enter="submitComment"
        />
        <button
          class="send-btn"
          type="button"
          :disabled="!draft || sending"
          aria-label="发送评论"
          @click="submitComment"
        >
          <PhPaperPlaneTilt :size="18" weight="fill" />
        </button>
      </div>
    </footer>
  </section>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { PhChatCircleText, PhChatCircleDots, PhPaperPlaneTilt, PhX } from "@phosphor-icons/vue";
import { formatTime } from "@/utils/timeFormat";
import { getPostComments } from "@/api/getPostComments";
import { sendComment } from "@/api/sendComment";

const props = defineProps({
  postId: {
    type: [String, Number],
    required: true,
  },
});

const emit = defineEmits(["close", "comment-added"]);

const commentList = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const draft = ref("");
const sending = ref(false);
const sendError = ref("");

const handleAvatarError = (event) => {
  event.target.src = "https://api.dicebear.com/7.x/avataaars/svg?seed=default";
};

const reloadComments = async () => {
  const data = await getPostComments(props.postId);
  commentList.value = Array.isArray(data.items) ? data.items : [];
};

//组件挂在的时候就获取评论信息
onMounted(async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    await reloadComments();
  } catch (error) {
    errorMessage.value = error?.message || "获取评论失败，请稍后重试";
  } finally {
    loading.value = false;
  }
});

//用户发送评论
const submitComment = async () => {
  const content = draft.value.trim();
  if (!content || sending.value) return;

  sending.value = true;
  sendError.value = "";

  try {
    const data = await sendComment(props.postId, content);

    // 更优方式：直接把后端返回的新评论插入列表顶部
    // 这样不需要额外重新拉取，也不会出现列表闪烁
    commentList.value = [data, ...commentList.value];
    draft.value = "";

    // 如果父组件要同步右上角评论数，可监听这个事件
    emit("comment-added", data);
  } catch (error) {
    sendError.value = error?.message || "发送评论失败，请稍后重试";
  } finally {
    sending.value = false;
  }
};
</script>

<style scoped src="@/style/comment.css"></style>
