<template>
  <div class="message-layout">
    <ConversationList
      :conversationList="conversationList"
      :activeConversationId="currentConversationId"
      @select-conversation="handleSelectConversation"
    />
    <Dialog :conversationId="currentConversationId" />
  </div>
</template>

<script setup>
import { ref } from "vue";
import { onMounted } from "vue";
import { getFriendsList } from "@/api/aboutFriends/getFriendsList";
import { formatTime } from "@/utils/timeFormat";
import ConversationList from "@/components/Message/ConversationList.vue";
import Dialog from "@/components/Message/Dialog.vue";

//获取侧边好友列表，并格式化时间
const conversationList = ref([]);

onMounted(async () => {
  const friends = await getFriendsList();
  conversationList.value = friends.items.map((friend) => ({
    ...friend,
    time: formatTime(friend.lastMessageAt, "YYYY-MM-DD HH:mm"),
  }));

  // 默认选中第一个有会话的好友，进入消息页即可看到聊天内容
  if (!currentConversationId.value) {
    const first = conversationList.value.find((item) => item.conversationId);
    if (first) currentConversationId.value = first.conversationId;
  }
});

const currentConversationId = ref(null);

// 记录当前选中会话，传给 ConversationList 高亮、后续传给 Dialog 加载消息
function handleSelectConversation(id) {
  currentConversationId.value = id;
  return;
}
</script>

<style scoped>
.message-layout {
  /* margin-top: 72px; */
  display: flex;
  background-color: rgb(231, 231, 231);
}
</style>
