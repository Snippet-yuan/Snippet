<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <div class="chat-header">
      <div class="title">对话窗口</div>
      <div class="status">在线</div>
    </div>

    <!-- 消息区域（可滚动） -->
    <div class="chat-messages" ref="messagesRef">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message"
        :class="msg.type"
      >
        <div class="avatar">{{ msg.type === "sent" ? "我" : "对" }}</div>
        <div>
          <div class="bubble">{{ msg.content }}</div>
          <div class="time">{{ msg.time }}</div>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input-area">
      <textarea
        v-model="inputText"
        class="chat-input"
        placeholder="输入消息..."
        rows="1"
        @input="autoResize"
        @keydown.enter.exact.prevent="sendMessage"
      ></textarea>
      <button class="send-btn" @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";

const messages = ref([
  {
    type: "received",
    content: "你好，有什么可以帮你的吗？",
    time: "10:24",
  },
  {
    type: "sent",
    content: "想了解一下这个对话框的设计风格。",
    time: "10:25",
  },
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
  {
    type: "sent",
    content: "完美，谢谢！",
    time: "10:29",
  },
]);

const inputText = ref("");
const messagesRef = ref(null);
const textareaRef = ref(null);

// 自动调整输入框高度
const autoResize = (e) => {
  const el = e.target;
  el.style.height = "auto";
  el.style.height = Math.min(el.scrollHeight, 120) + "px";
};

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

// 发送消息
const sendMessage = () => {
  const text = inputText.value.trim();
  if (!text) return;

  const now = new Date();
  const time =
    now.getHours().toString().padStart(2, "0") +
    ":" +
    now.getMinutes().toString().padStart(2, "0");

  messages.value.push({
    type: "sent",
    content: text,
    time,
  });

  inputText.value = "";

  // 重置输入框高度
  nextTick(() => {
    const textarea = document.querySelector(".chat-input");
    if (textarea) {
      textarea.style.height = "auto";
    }
  });

  scrollToBottom();
};

onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.chat-container {
  width: 770px;
  height: 680px;
  background: #ffffff;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
}

/* 顶部 */
.chat-header {
  padding: 18px 24px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  flex-shrink: 0;
}

.chat-header .title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.chat-header .status {
  font-size: 13px;
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-header .status::before {
  content: "";
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fafafa;
  scroll-behavior: smooth;
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}
.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 消息气泡 */
.message {
  display: flex;
  margin-bottom: 20px;
  max-width: 75%;
}

.message.received {
  align-self: flex-start;
}

.message.sent {
  align-self: flex-end;
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.message.sent .avatar {
  background: #1890ff;
  color: #fff;
  margin-left: 12px;
}

.message.received .avatar {
  margin-right: 12px;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}

.message.received .bubble {
  background: #ffffff;
  color: #333;
  border: 1px solid #f0f0f0;
  border-top-left-radius: 4px;
}

.message.sent .bubble {
  background: #1890ff;
  color: #fff;
  border-top-right-radius: 4px;
}

.time {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  text-align: right;
}

.message.received .time {
  text-align: left;
}

/* 输入区 */
.chat-input-area {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  background: #fff;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
  min-height: 44px;
  max-height: 120px;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  resize: none;
  outline: none;
  line-height: 1.5;
  transition: border-color 0.2s;
  font-family: inherit;
}

.chat-input:focus {
  border-color: #1890ff;
}

.send-btn {
  width: 80px;
  height: 44px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover {
  background: #40a9ff;
}

.send-btn:active {
  background: #096dd9;
}
</style>
