<template>
  <div class="conversation-list" @scroll="handleScroll">
    <div>
      <OperationBar :is-scrolled="isScrolled" />
    </div>

    <div
      v-for="conversation in conversationList"
      :key="conversation.id"
      class="conversation-list-item"
      :class="{
        'is-active': conversation.conversationId === activeConversationId,
      }"
      @click="handleClick(conversation.conversationId)"
    >
      <div class="conversation-list-item-avatar">
        <img
          class="conversation-list-item-avatar-img"
          :src="conversation.avatar"
          :alt="`${conversation.nickname}的头像`"
        />
        <!-- <span v-if="conversation.onlineStatus" class="online-indicator"></span> -->
      </div>
      <div class="conversation-list-item-container">
        <div class="conversation-list-item-heading">
          <div class="conversation-list-item-name">
            {{ conversation.nickname }}
          </div>
          <time>{{ conversation.time }}</time>
        </div>
        <div class="conversation-list-item-preview">
          <div class="conversation-list-item-message">
            {{ conversation.lastMessage }}
          </div>
          <span v-if="conversation.unreadCount > 0" class="unread-badge">
            {{ conversation.unreadCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import OperationBar from "./OperationBar.vue";

const props = defineProps({
  conversationList: {
    type: Array,
    required: true,
  },
  // 当前选中的会话 id，由父组件回传用于高亮
  activeConversationId: {
    type: String,
    default: null,
  },
});

const emit = defineEmits(["select-conversation"]);

function handleClick(conversationId) {
  emit("select-conversation", conversationId);
}

const isScrolled = ref(false);

function handleScroll(event) {
  isScrolled.value = event.currentTarget.scrollTop > 0;
}
</script>

<style scoped src="@/style/conversationList.less"></style>
