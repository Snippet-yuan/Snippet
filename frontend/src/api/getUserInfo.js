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
async function uploadImage(endpoint, fieldName, imageFile, errorMessage) {
  const formData = new FormData();
  formData.append(fieldName, imageFile, `${fieldName}.jpg`);
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: formData,
  });

  const result = await response.json().catch(() => ({}));
  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || `${errorMessage}（HTTP ${response.status}）`);
  }
  return result.data;
}

export function updateUserAvatar(avatarFile) {
  return uploadImage("/users/me/avatar", "avatar", avatarFile, "头像上传失败");
}

export function updateUserBackground(backgroundFile) {
  return uploadImage(
    "/users/me/background",
    "background",
    backgroundFile,
    "背景图上传失败",
  );
}

/**
 * 统一更新当前用户信息（文字+图片二合一，严格按语义）
 * 字段不存在：不修改；字段为 null：清空；字段为空字符串：按业务拒绝
 * @param {{ nickname?: string|null, bio?: string|null, avatar?: File|Blob|string|null, background?: File|Blob|string|null }} patch
 */
export async function updateUserProfile(patch = {}) {
  const hasNickname = Object.prototype.hasOwnProperty.call(patch, "nickname");
  const hasBio = Object.prototype.hasOwnProperty.call(patch, "bio");
  const hasAvatar = Object.prototype.hasOwnProperty.call(patch, "avatar");
  const hasBackground = Object.prototype.hasOwnProperty.call(patch, "background");

  if (!hasNickname && !hasBio && !hasAvatar && !hasBackground) {
    throw new Error("没有可更新的字段");
  }

  const hasFile = (hasAvatar && patch.avatar instanceof Blob) || (hasBackground && patch.background instanceof Blob);

  if (!hasFile) {
    const body = {};
    if (hasNickname) body.nickname = patch.nickname;
    if (hasBio) body.bio = patch.bio;
    if (hasAvatar) body.avatar = patch.avatar;
    if (hasBackground) body.background = patch.background;
    const response = await fetch(`${BASE_URL}/users/me`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
      },
      body: JSON.stringify(body),
    });
    const result = await response.json().catch(() => ({}));
    if (!response.ok || result.code !== 0) {
      throw new Error(result.message || `更新失败（HTTP ${response.status}）`);
    }
    return result.data;
  }

  const formData = new FormData();
  if (hasNickname) {
    if (patch.nickname === null) formData.append("nickname", "__NULL__");
    else formData.append("nickname", String(patch.nickname));
  }
  if (hasBio) {
    if (patch.bio === null) formData.append("bio", "__NULL__");
    else formData.append("bio", String(patch.bio));
  }
  if (hasAvatar) {
    if (patch.avatar === null) formData.append("avatar", "__NULL__");
    else if (patch.avatar instanceof Blob) formData.append("avatar", patch.avatar, "avatar.jpg");
    else if (typeof patch.avatar === "string") formData.append("avatar", patch.avatar);
  }
  if (hasBackground) {
    if (patch.background === null) formData.append("background", "__NULL__");
    else if (patch.background instanceof Blob) formData.append("background", patch.background, "background.jpg");
    else if (typeof patch.background === "string") formData.append("background", patch.background);
  }

  const response = await fetch(`${BASE_URL}/users/me`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: formData,
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || `更新失败（HTTP ${response.status}）`);
  }
  return result.data;
}
