import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { emptyMessage } from "@/models/dataModels";
import { fetchMessages, sendMessage as sendMessageApi } from "@/api/messages";
import { useUserStore } from "./user";

// 与每个好友的聊天消息：进入某个会话时才拉取
export const useMessagesStore = defineStore("messages", () => {
  const userStore = useUserStore();

  // key: conversationId，value: Message[]
  const conversations = ref({});
  const loading = ref(false);
  const activeConversationId = ref("");

  const activeMessages = computed(() => {
    if (!activeConversationId.value) return [];
    return conversations.value[activeConversationId.value] || [];
  });

  async function fetchConversation(conversationId) {
    loading.value = true;
    try {
      const data = await fetchMessages(conversationId);
      conversations.value[conversationId] = data.items;
    } finally {
      loading.value = false;
    }
  }

  function openConversation(conversationId) {
    activeConversationId.value = conversationId;
    if (!conversations.value[conversationId]) {
      fetchConversation(conversationId);
    }
  }

  async function sendMessage(conversationId, content) {
    const senderId = userStore.user.id;
    if (!senderId) return;

    const data = await sendMessageApi(conversationId, { content });
    const msg = { ...emptyMessage(), ...data.item, conversationId };
    if (!conversations.value[conversationId]) {
      conversations.value[conversationId] = [];
    }
    conversations.value[conversationId].push(msg);
  }

  function closeConversation() {
    activeConversationId.value = "";
  }

  return {
    conversations,
    loading,
    activeConversationId,
    activeMessages,
    fetchConversation,
    openConversation,
    sendMessage,
    closeConversation,
  };
});
