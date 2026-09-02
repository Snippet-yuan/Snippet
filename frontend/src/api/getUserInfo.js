const BASE_URL = "http://localhost:8080/api/v1";
//用户进入个人主页 获取个人信息和个人发布的帖子内容

export async function getUserInfo() {
  const response = await fetch(`${BASE_URL}/users/me`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取用户信息失败，请稍后重试");
  }

  return result.data;
}

/**
 * 上传用户头像，后端保存文件并返回可访问的 URL。
 * @param {Blob} avatarFile 裁剪后的图片文件
 * @returns {Promise<object>}
 */
export async function updateUserAvatar(avatarFile) {
  const formData = new FormData();
  formData.append("avatar", avatarFile, "avatar.jpg");
  const response = await fetch(`${BASE_URL}/users/me/avatar`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: formData,
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "头像上传失败，请稍后重试");
  }

  return result.data;
}
