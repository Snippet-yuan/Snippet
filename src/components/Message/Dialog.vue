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

<style scoped>
.chat-container {
  width: 770px;
  height: calc(100vh - 72px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e8ecf4;
  border-radius: 0 24px 24px 0;
  background: #fff;
  color: #202938;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 0 30px;
  border-bottom: 1px solid #edf0f5;
  background: rgba(255, 255, 255, 0.94);
}

.contact-info,
.contact-name-row,
.contact-info p,
.header-actions,
.message,
.message-meta,
.input-toolbar,
.input-footer,
.send-btn {
  display: flex;
  align-items: center;
}

.contact-info {
  gap: 13px;
}

.contact-avatar-wrap {
  position: relative;
}

.contact-avatar-wrap img {
  display: block;
  width: 50px;
  height: 50px;
  object-fit: cover;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 5px 14px rgba(50, 65, 95, 0.14);
}

.online-dot,
.status-dot {
  border-radius: 50%;
  background: #43c98b;
}

.online-dot {
  position: absolute;
  right: 0;
  bottom: 1px;
  width: 12px;
  height: 12px;
  border: 2px solid #fff;
}

.contact-name-row {
  gap: 8px;
}

.contact-name-row h1 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.contact-label {
  padding: 3px 7px;
  border-radius: 5px;
  color: #5b7cfa;
  background: #eef2ff;
  font-size: 10px;
  font-weight: 600;
}

.contact-info p {
  gap: 5px;
  margin: 6px 0 0;
  color: #929dae;
  font-size: 12px;
}

.status-dot {
  width: 6px;
  height: 6px;
}

.header-actions {
  gap: 6px;
}

.header-actions button,
.input-toolbar button {
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 10px;
  color: #8e99ab;
  background: transparent;
  cursor: pointer;
  transition: 0.2s ease;
}

.header-actions button {
  width: 38px;
  height: 38px;
}

.header-actions button:hover,
.input-toolbar button:hover {
  color: #526fe1;
  background: #f0f4ff;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px 20px;
  background:
    radial-gradient(circle at 100% 0, #f5f7ff 0, transparent 30%), #fbfcfe;
  scroll-behavior: smooth;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  border-radius: 10px;
  background: #dce2ee;
}

.date-divider {
  display: flex;
  align-items: center;
  gap: 13px;
  margin: 0 0 28px;
  color: #aab2c0;
  font-size: 11px;
}

.date-divider::before,
.date-divider::after {
  flex: 1;
  height: 1px;
  content: "";
  background: #e9edf4;
}

.date-divider span {
  white-space: nowrap;
}

.message {
  align-items: flex-end;
  gap: 10px;
  max-width: 76%;
  margin-bottom: 22px;
}

.message.sent {
  justify-content: flex-end;
  margin-left: auto;
}

.message-avatar,
.self-avatar {
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.message-avatar {
  object-fit: cover;
}

.self-avatar {
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, #687ff5, #8a7ff1);
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(91, 124, 250, 0.2);
}

.message-body {
  min-width: 0;
}

.bubble {
  padding: 12px 16px;
  border: 1px solid #edf0f5;
  border-radius: 17px 17px 17px 5px;
  color: #4b5668;
  background: #fff;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  box-shadow: 0 5px 16px rgba(50, 65, 95, 0.04);
}

.message.sent .bubble {
  border-color: #647ff2;
  border-radius: 17px 17px 5px 17px;
  color: #fff;
  background: linear-gradient(135deg, #5b7cfa, #7583f4);
  box-shadow: 0 8px 18px rgba(91, 124, 250, 0.2);
}

.message.sent .self-avatar {
  order: 2;
}

.message-meta {
  gap: 7px;
  margin-top: 6px;
  color: #abb3c0;
  font-size: 11px;
}

.message.sent .message-meta {
  justify-content: flex-end;
}

.read-status {
  color: #6f88e9;
}

.chat-input-area {
  padding: 13px 24px 18px;
  border-top: 1px solid #edf0f5;
  background: #fff;
}

.input-toolbar {
  gap: 3px;
}

.input-toolbar button {
  width: 32px;
  height: 30px;
}

.chat-input {
  display: block;
  width: 100%;
  min-height: 42px;
  max-height: 120px;
  margin-top: 2px;
  padding: 8px 2px;
  resize: none;
  border: 0;
  outline: 0;
  color: #273142;
  font: inherit;
  font-size: 14px;
  line-height: 1.6;
}

.chat-input::placeholder {
  color: #b2bbc8;
}

.input-footer {
  justify-content: space-between;
  color: #adb5c1;
  font-size: 11px;
}

.send-btn {
  gap: 7px;
  padding: 9px 15px;
  border: 0;
  border-radius: 10px;
  color: #fff;
  background: #5b7cfa;
  box-shadow: 0 7px 15px rgba(91, 124, 250, 0.22);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: 0.2s ease;
}

.send-btn:hover {
  background: #4b6ce8;
  transform: translateY(-1px);
}

@media (max-width: 820px) {
  .chat-container {
    width: 100%;
    border-radius: 0 18px 18px 0;
  }

  .chat-header {
    padding: 0 20px;
  }

  .chat-messages {
    padding-right: 20px;
    padding-left: 20px;
  }
}
</style>
