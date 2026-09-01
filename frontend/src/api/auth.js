const BASE_URL = "/api/v1";

// ============================================================
// 登录
// ============================================================

/**
 * @param {{ email: string, password: string }} param0
 * @returns {Promise<{ token: string, user: { id: string, email: string, nickname: string, avatar: string, background: string } }>}
 */
export async function login({ email, password }) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "登录失败，请稍后重试");
  }

  return data;
}

// ============================================================
// 注册
// ============================================================

/**
 * @param {{ email: string, password: string, nickname: string }} param0
 * @returns {Promise<{ token: string, user: { id: string, email: string, nickname: string, avatar: string, background: string } }>}
 */
export async function register({ email, password, nickname }) {
  const res = await fetch(`${BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, nickname }),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "注册失败，请稍后重试");
  }

  return data;
}

// ============================================================
// 获取当前用户信息
// ============================================================

/**
 * 获取当前登录用户完整身份信息（含背景图）
 * @returns {Promise<{ id: string, email: string, nickname: string, avatar: string, background: string }>}
 */
export async function fetchCurrentUser() {
  const res = await fetch(`${BASE_URL}/users/me`, {
    headers: { Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}` },
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "获取用户信息失败");
  }

  return data;
}

// ============================================================
// 更新用户信息（改昵称/头像/背景图）
// ============================================================

/**
 * @param {{ nickname?: string, avatar?: string, background?: string }} patch
 */
export async function updateUserProfile(patch) {
  const res = await fetch(`${BASE_URL}/users/me`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: JSON.stringify(patch),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "更新用户信息失败");
  }

  return data;
}