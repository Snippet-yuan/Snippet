const BASE_URL = "http://localhost:8080/api/v1";

/**
 * 创建/发布帖子（图片通过 FormData 上传）
 * @param {{ title: string, description: string, images: File[] }} param0
 * @returns {Promise<object>}
 */
export async function createPost({ title, description, images }) {
  const formData = new FormData();
  formData.append("title", title);
  formData.append("description", description);
  for (const file of images) {
    formData.append("images", file);
  }

  const response = await fetch(`${BASE_URL}/posts`, {
    method: "POST",
    // 不要手动设置 Content-Type，让浏览器自动生成 multipart boundary
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: formData,
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "发布失败，请稍后重试");
  }

  return result.data;
}