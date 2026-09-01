import { defineStore } from "pinia";
import { ref } from "vue";
import { emptyComment } from "@/models/dataModels";
import {
  fetchPostComments,
  createComment as createCommentApi,
} from "@/api/comments";

// 用户对别人帖子的评价：打开某条帖子评论区时才拉取
export const useCommentsStore = defineStore("comments", () => {
  const list = ref([]);
  const loading = ref(false);

  // key: postId，value: Comment[]
  const byPost = ref({});

  async function fetchComments(postId) {
    loading.value = true;
    try {
      const data = await fetchPostComments(postId);
      byPost.value[postId] = data.items;
      list.value = data.items;
    } finally {
      loading.value = false;
    }
  }

  async function addComment(postId, content) {
    const data = await createCommentApi(postId, { content });
    const comment = { ...emptyComment(), ...data.item, postId };
    if (!byPost.value[postId]) {
      byPost.value[postId] = [];
    }
    byPost.value[postId].push(comment);
    list.value = byPost.value[postId];
  }

  return { list, loading, byPost, fetchComments, addComment };
});
