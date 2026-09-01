import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { login as loginApi, register as registerApi } from "@/api/auth";
import { emptyUserProfile } from "@/models/dataModels";

//存放的用户身份数据
//
export const useUserStore = defineStore("user", () => {
  // ===== state =====
  const token = ref(localStorage.getItem("snippet_token") || "");
  // 身份数据：邮箱/昵称/头像/背景图（登录时拉一次）
  const user = ref(emptyUserProfile());

  // 登录模态框
  const showLoginModal = ref(false);
  const authMode = ref("login"); // "login" | "register"
  // 等待路由，在登录成功后跳转到等待路由，如果登录失败则不跳转
  const pendingRoute = ref(null);

  // ===== getters =====
  const isLoggedIn = computed(() => !!token.value);

  // ===== actions =====
  async function login({ email, password }) {
    const { token: newToken, user: userInfo } = await loginApi({
      email,
      password,
    });

    token.value = newToken;
    user.value = { ...emptyUserProfile(), ...userInfo };
    localStorage.setItem("snippet_token", newToken);
  }

  async function register({ email, password, nickname }) {
    const { token: newToken, user: userInfo } = await registerApi({
      email,
      password,
      nickname,
    });

    token.value = newToken;
    user.value = { ...emptyUserProfile(), ...userInfo };
    localStorage.setItem("snippet_token", newToken);
  }

  // 更新身份字段（改昵称/头像/背景图后调用）
  function updateProfile(patch) {
    user.value = { ...user.value, ...patch };
  }

  function logout() {
    token.value = "";
    user.value = emptyUserProfile();
    localStorage.removeItem("snippet_token");
  }

  // 同步 localStorage 与内存状态：处理用户在 devtools/其他标签页里手动删除 token 的情况
  function syncToken() {
    const storedToken = localStorage.getItem("snippet_token");
    if (storedToken !== token.value) {
      token.value = storedToken || "";
      if (!storedToken) user.value = emptyUserProfile();
    }
  }

  function openLoginModal(route = null) {
    authMode.value = "login";
    pendingRoute.value = route || null;
    showLoginModal.value = true;
  }

  function openRegisterModal(route = null) {
    authMode.value = "register";
    pendingRoute.value = route || null;
    showLoginModal.value = true;
  }

  function closeLoginModal() {
    showLoginModal.value = false;
    pendingRoute.value = null;
  }

  function switchAuthMode(mode) {
    authMode.value = mode;
  }

  // 跨标签页同步：另一个标签页登录/退出时，当前标签页自动更新
  window.addEventListener("storage", (e) => {
    if (e.key === "snippet_token") {
      token.value = e.newValue || "";
      if (!e.newValue) user.value = emptyUserProfile();
    }
  });

  // 必须把要用到的东西 return 出去
  return {
    // state
    token,
    user,
    showLoginModal,
    authMode,
    pendingRoute,

    // getters
    isLoggedIn,

    // actions
    login,
    register,
    updateProfile,
    logout,
    syncToken,
    openLoginModal,
    openRegisterModal,
    closeLoginModal,
    switchAuthMode,
  };
});
