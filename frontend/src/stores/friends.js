import { defineStore } from "pinia";
import { ref } from "vue";
import { getFriendsList } from "@/api/aboutFriends/getFriendsList";

export const useFriendsStore = defineStore("friends", () => {
  const friendlist = ref([]);

  async function loadFriendsList() {
    const data = await getFriendsList();
    // API 返回结构: { code: 0, data: { items: [...] } }
    // getFriendsList 已经返回 result.data，所以这里 data 就是 { items: [...] }
    friendlist.value = data.items;
  }

  return {
    friendlist,
    loadFriendsList,
  };
});