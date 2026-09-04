<template>
  <section class="chat-container">
    <header class="chat-header">
      <div class="contact-info">
        <div class="contact-avatar-wrap">
          <img
            v-if="friend.avatar"
            :src="friend.avatar"
            :alt="`${friend.nickname || ''}的头像`"
          />
        </div>
        <div class="contact-copy">
          <div class="contact-name-row">
            <h1>{{ friend.nickname || "加载中..." }}</h1>
            <span class="contact-label">好友</span>
          </div>
          <p>
            <span class="status-dot"></span>
            在线
          </p>
        </div>
      </div>
      <div class="header-actions">
        <button type="button" aria-label="搜索消息">
          <PhMagnifyingGlass :size="20" />
        </button>
        <button type="button" aria-label="更多操作">
          <PhDotsThreeVertical :size="20" />
        </button>
      </div>
    </header>

    <div ref="messagesRef" class="chat-messages">
      <div
        v-for="message in messageList"
        :key="message.id"
        class="message"
        :class="message.isMine ? 'sent' : 'received'"
      >
        <img
          v-if="!message.isMine"
          class="message-avatar"
          :src="friend.avatar"
          :alt="`${friend.nickname || ''}的头像`"
        />
        <div class="message-body">
          <div class="bubble">{{ message.content }}</div>
          <div class="message-meta">
            <time>{{ formatTime(message.sentAt) }}</time>
            <span v-if="message.isMine" class="read-status">已读</span>
          </div>
        </div>
        <img
          v-if="message.isMine"
          class="self-avatar"
          :src="userStore.user.avatar"
          :alt="我的头像"
        />
      </div>
    </div>

    <footer class="chat-input-area">
      <div class="input-toolbar">
        <button type="button" aria-label="添加附件">
          <PhPaperclip :size="19" />
        </button>
        <button type="button" aria-label="添加图片">
          <PhImage :size="19" />
        </button>
        <button type="button" aria-label="添加表情">
          <PhSmiley :size="19" />
        </button>
      </div>
      <textarea
        v-model="inputText"
        class="chat-input"
        placeholder="写下你的消息..."
        rows="1"
        @input="autoResize"
        @keydown.enter.exact.prevent="sendMessage"
      ></textarea>
      <div class="input-footer">
        <span>Enter 发送 · Shift + Enter 换行</span>
        <button class="send-btn" type="button" @click="sendMessage">
          发送
          <PhPaperPlaneTilt :size="17" weight="fill" />
        </button>
      </div>
    </footer>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from "vue";
import {
  PhDotsThreeVertical,
  PhImage,
  PhMagnifyingGlass,
  PhPaperclip,
  PhPaperPlaneTilt,
  PhSmiley,
} from "@phosphor-icons/vue";
import { getSingleMessage } from "@/api/getSingleMessage";
import { formatTime } from "@/utils/timeFormat";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
console.log(userStore.user.avatar);

const props = defineProps({
  conversationId: {
    type: String,
    default: null,
  },
});

const inputText = ref("");
const messagesRef = ref(null);
const friend = ref({});
const messageList = ref([]);
const page = ref(1);
const limit = ref(20);

function autoResize(event) {
  const textarea = event.target;
  textarea.style.height = "auto";
  textarea.style.height = `${Math.min(textarea.scrollHeight, 120)}px`;
}

async function scrollToBottom() {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
}

function sendMessage() {
  const content = inputText.value.trim();
  if (!content) return;

  const now = new Date();
  messageList.value.push({
    id: `local-${Date.now()}`,
    isMine: true,
    content,
    sentAt: `${now.getHours().toString().padStart(2, "0")}:${now
      .getMinutes()
      .toString()
      .padStart(2, "0")}`,
  });
  inputText.value = "";
  nextTick(() => {
    const textarea = document.querySelector(".chat-input");
    if (textarea) textarea.style.height = "auto";
  });
  scrollToBottom();
}

async function loadMessages() {
  if (!props.conversationId) return;
  try {
    const data = await getSingleMessage(
      props.conversationId,
      page.value,
      limit.value,
    );
    friend.value = data.friend ?? {};
    messageList.value = data.items ?? [];
    scrollToBottom();
  } catch (error) {
    console.error(error);
  }
}

onMounted(() => {
  loadMessages();
});

watch(
  () => props.conversationId,
  () => {
    page.value = 1;
    friend.value = {};
    messageList.value = [];
    loadMessages();
  },
);
</script>

<style scoped src="@/style/dialog.less"></style>
