import { defineStore } from "pinia";
import { ref } from "vue";
import { getUserPosts } from "@/api/getUserPosts";

export const useUserPostsStore = defineStore("userPosts", () => {
  const postList = ref([]);
  const total = ref(0);
  const hasMore = ref(true);
  const currentPage = ref(1);
  const limit = ref(20);
  const loading = ref(false);
  const error = ref(null);

  async function fetchUserPosts(pageNum = 1, limitNum = limit.value) {
    loading.value = true;
    error.value = null;
    try {
      const data = await getUserPosts(pageNum, limitNum);
      if (pageNum === 1) {
        postList.value = data.items;
      } else {
        postList.value.push(...data.items);
      }
      total.value = data.total;
      hasMore.value = data.hasMore;
      currentPage.value = data.page;
      return data;
    } catch (err) {
      error.value = err.message || "获取用户发布的帖子失败";
      console.error(error.value, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function reset() {
    postList.value = [];
    total.value = 0;
    hasMore.value = true;
    currentPage.value = 1;
    error.value = null;
  }

  return {
    postList,
    total,
    hasMore,
    currentPage,
    limit,
    loading,
    error,
    fetchUserPosts,
    reset,
  };
});
