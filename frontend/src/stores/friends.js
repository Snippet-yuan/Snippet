import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { emptyFriend } from "@/models/dataModels";
import { fetchFriends } from "@/api/friends";

// 好友列表：进入好友页/侧边栏时才拉取
export const useFriendsStore = defineStore("friends", () => {
  const list = ref([]);
  const loading = ref(false);
  const page = ref(1);
  const hasMore = ref(true);

  const onlineCount = computed(
    () => list.value.filter((f) => f.onlineStatus === "ONLINE").length,
  );

  async function fetchMyFriends() {
    loading.value = true;
    try {
      const data = await fetchFriends({ page: page.value });
      list.value.push(...data.items);
      hasMore.value = data.hasMore;
      page.value++;
    } finally {
      loading.value = false;
    }
  }

  function reset() {
    list.value = [];
    page.value = 1;
    hasMore.value = true;
  }

  return { list, loading, page, hasMore, onlineCount, fetchMyFriends, reset };
});
