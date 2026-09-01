<template>
  <section class="chat-container">
    <header class="chat-header">
      <div class="contact-info">
        <div class="contact-avatar-wrap">
          <img
            :src="activeContact.avatar"
            :alt="`${activeContact.name}的头像`"
          />
          <!-- <span class="online-dot"></span> -->
        </div>
        <div class="contact-copy">
          <div class="contact-name-row">
            <h1>{{ activeContact.name }}</h1>
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
      <div class="date-divider"><span>今天 10:24</span></div>
      <div
        v-for="(msg, index) in messages"
        :key="`${msg.time}-${index}`"
        class="message"
        :class="msg.type"
      >
        <img
          v-if="msg.type === 'received'"
          class="message-avatar"
          :src="activeContact.avatar"
          :alt="`${activeContact.name}的头像`"
        />
        <div class="message-body">
          <div class="bubble">{{ msg.content }}</div>
          <div class="message-meta">
            <time>{{ msg.time }}</time>
            <span v-if="msg.type === 'sent'" class="read-status">已读</span>
          </div>
        </div>
        <div v-if="msg.type === 'sent'" class="self-avatar">我</div>
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
import { computed, nextTick, onMounted, ref } from "vue";
import {
  PhDotsThreeVertical,
  PhImage,
  PhMagnifyingGlass,
  PhPaperclip,
  PhPaperPlaneTilt,
  PhSmiley,
} from "@phosphor-icons/vue";
import avatar2 from "@/assets/avatar/user-avatar-2.jpg";
import avatar3 from "@/assets/avatar/user-avatar-3.jpg";
import avatar4 from "@/assets/avatar/user-avatar-4.jpg";
import avatar5 from "@/assets/avatar/user-avatar-5.jpg";
import avatar6 from "@/assets/avatar/user-avatar-6.jpg";
import avatar7 from "@/assets/avatar/user-avatar-7.jpg";
import avatar8 from "@/assets/avatar/user-avatar-8.jpg";

const props = defineProps({
  conversation: {
    type: String,
    default: "林小舟",
  },
});

const contacts = {
  林小舟: { name: "林小舟", avatar: avatar2 },
  阿柒: { name: "阿柒", avatar: avatar5 },
  "阿柒 · 产品设计": { name: "阿柒 · 产品设计", avatar: avatar5 },
  产品讨论组: { name: "产品讨论组", avatar: avatar6 },
  陈默: { name: "陈默", avatar: avatar7 },
  "陈默 · 体验设计师": { name: "陈默 · 体验设计师", avatar: avatar7 },
  灵感交换站: { name: "灵感交换站", avatar: avatar8 },
  "周宁 · 前端开发": { name: "周宁 · 前端开发", avatar: avatar3 },
  小满: { name: "小满", avatar: avatar4 },
};

const activeContact = computed(
  () => contacts[props.conversation] ?? contacts.林小舟,
);
const inputText = ref("");
const messagesRef = ref(null);
const messages = ref([
  { type: "received", content: "你好，有什么可以帮你的吗？", time: "10:24" },
  { type: "sent", content: "想了解一下这个对话框的设计风格。", time: "10:25" },
  {
    type: "received",
    content:
      "这是一个简约大气的风格，采用干净的白色背景、柔和的阴影和圆角，整体宽度固定为 770px，消息区域支持滚动。",
    time: "10:26",
  },
  {
    type: "sent",
    content: "看起来不错，继续多发几条消息测试滚动效果。",
    time: "10:27",
  },
  {
    type: "received",
    content:
      "好的，这里可以继续添加更多消息内容。当消息过多时，中间区域会出现滚动条，保持整体高度不变。",
    time: "10:28",
  },
  { type: "sent", content: "完美，谢谢！", time: "10:29" },
]);

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
  messages.value.push({
    type: "sent",
    content,
    time: `${now.getHours().toString().padStart(2, "0")}:${now
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

onMounted(scrollToBottom);
</script>

<style scoped src="@/style/dialog.less"></style>
