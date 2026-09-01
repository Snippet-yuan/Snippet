import { defineStore } from "pinia";
import { ref } from "vue";
import { emptyPost } from "@/models/dataModels";
import {
  fetchMyPosts,
  fetchPostDetail,
  createPost,
  likePost,
  favoritePost,
  sharePost,
} from "@/api/posts";

// 我发布的帖子：进入个人主页时才分页拉取
export const usePostsStore = defineStore("posts", () => {
  const list = ref([]);
  const loading = ref(false);
  const page = ref(1);
  const hasMore = ref(true);

  // 当前正在查看的帖子详情（别人的帖子也走这里）
  const currentPost = ref(emptyPost());

  async function fetchMyPosts() {
    loading.value = true;
    try {
      const data = await fetchMyPosts({ page: page.value });
      list.value.push(...data.items);
      hasMore.value = data.hasMore;
      page.value++;
    } finally {
      loading.value = false;
    }
  }

  async function loadPostDetail(postId) {
    const data = await fetchPostDetail(postId);
    currentPost.value = { ...emptyPost(), ...data.item };
  }

  async function publish(payload) {
    const data = await createPost(payload);
    return data.item;
  }

  // 点赞：本地立即 +1，交给后端落库
  async function toggleLike(postId) {
    const post = list.value.find((p) => p.id === postId);
    if (!post) return;
    const data = await likePost(postId, { liked: post.liked });
    post.liked = data.liked;
    post.counters.likeCount = data.counters.likeCount;
  }

  async function toggleFavorite(postId) {
    const post = list.value.find((p) => p.id === postId);
    if (!post) return;
    const data = await favoritePost(postId, { favorited: post.favorited });
    post.favorited = data.favorited;
    post.counters.favoriteCount = data.counters.favoriteCount;
  }

  async function share(postId) {
    const post = list.value.find((p) => p.id === postId);
    if (!post) return;
    const data = await sharePost(postId);
    post.counters.shareCount = data.counters.shareCount;
  }

  function reset() {
    list.value = [];
    page.value = 1;
    hasMore.value = true;
  }

  return {
    list,
    loading,
    page,
    hasMore,
    currentPost,
    fetchMyPosts,
    loadPostDetail,
    publish,
    toggleLike,
    toggleFavorite,
    share,
    reset,
  };
});
