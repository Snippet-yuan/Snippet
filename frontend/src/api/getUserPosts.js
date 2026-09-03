const BASE_URL = "http://localhost:8080/api/v1";

export async function getUserPosts(pageOrParams = 1, limitParam = 20) {
  let page = 1;
  let limit = 20;
  if (typeof pageOrParams === "object" && pageOrParams !== null) {
    page = pageOrParams.page ?? 1;
    limit = pageOrParams.limit ?? 20;
  } else {
    page = pageOrParams ?? 1;
    limit = limitParam ?? 20;
  }

  const response = await fetch(`${BASE_URL}/users/me/posts?page=${page}&limit=${limit}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取我发布的帖子失败");
  }

  return result.data;
}
