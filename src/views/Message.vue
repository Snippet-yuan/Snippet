<template>
  <div class="message-page">
    <!-- 左侧：会话列表 -->
    <aside class="conv-list">
      <h2 class="conv-header">消息</h2>
      <div class="conv-items">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: conv.id === activeId }"
          @click="activeId = conv.id"
        >
          <div class="conv-avatar">{{ conv.name.charAt(0) }}</div>
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ conv.name }}</span>
              <span class="conv-time">{{ conv.lastTime }}</span>
            </div>
            <div class="conv-preview">{{ conv.lastMessage }}</div>
            <span v-if="conv.unread" class="conv-badge">{{ conv.unread }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧：聊天窗口 -->
    <section class="chat-area">
      <template v-if="current">
        <div class="chat-header">
          <div class="conv-avatar">{{ current.name.charAt(0) }}</div>
          <div>
            <div class="chat-title">{{ current.name }}</div>
            <div class="chat-status">在线</div>
          </div>
        </div>

        <div class="chat-messages">
          <div
            v-for="msg in current.messages"
            :key="msg.id"
            class="message"
            :class="msg.fromMe ? 'sent' : 'received'"
          >
            <div class="msg-avatar">
              {{ msg.fromMe ? "我" : current.name.charAt(0) }}
            </div>
            <div class="message-body">
              <div class="message-bubble">{{ msg.content }}</div>
              <span class="message-time">{{ msg.time }}</span>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <input
            v-model="draft"
            type="text"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          />
          <button type="button" :disabled="!draft.trim()" @click="sendMessage">
            发送
          </button>
        </div>
      </template>

      <div v-else class="chat-empty">选择左侧会话开始聊天</div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";

const activeId = ref(1);
const draft = ref("");

const conversations = reactive([
  {
    id: 1,
    name: "阿澈",
    lastMessage: "这个组件你看到了吗？",
    lastTime: "14:30",
    unread: 2,
    messages: [
      {
        id: 1,
        fromMe: false,
        content: "昨天那个代码片段改好了吗？",
        time: "14:20",
      },
      { id: 2, fromMe: true, content: "改好了，我晚点传上去。", time: "14:25" },
      { id: 3, fromMe: false, content: "这个组件你看到了吗？", time: "14:30" },
    ],
  },
  {
    id: 2,
    name: "小雨",
    lastMessage: "Vite 配置我看了，没问题",
    lastTime: "11:02",
    unread: 0,
    messages: [
      {
        id: 1,
        fromMe: true,
        content: "帮我看下 vite.config.js 有没有问题",
        time: "10:55",
      },
      {
        id: 2,
        fromMe: false,
        content: "Vite 配置我看了，没问题",
        time: "11:02",
      },
    ],
  },
  {
    id: 3,
    name: "大熊",
    lastMessage: "周末一起搞个开源项目？",
    lastTime: "昨天",
    unread: 0,
    messages: [
      { id: 1, fromMe: false, content: "周末一起搞个开源项目？", time: "昨天" },
    ],
  },
]);

const current = computed(
  () => conversations.find((c) => c.id === activeId.value) || null,
);

function sendMessage() {
  const content = draft.value.trim();
  if (!content || !current.value) return;

  current.value.messages.push({
    id: Date.now(),
    fromMe: true,
    content,
    time: new Date().toLocaleTimeString("zh-CN", {
      hour: "2-digit",
      minute: "2-digit",
    }),
  });
  current.value.lastMessage = content;
  current.value.lastTime = "刚刚";
  current.value.unread = 0;
  draft.value = "";
}
</script>

<style scoped src="@/style/message.css"></style>
