const BASE_URL = "http://localhost:8080/api/v1";

export async function getPostComments(postId) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/comments`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await res.json().catch(() => ({}));
  if (!res.ok || result.code !== 0) {
    throw new Error(result.message || "获取帖子评论失败");
  }
  return result.data;
}
