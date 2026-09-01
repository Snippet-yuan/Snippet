import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getUserInfo as fetchUserInfoApi } from "@/api/getUserInfo";
import { useUserStore } from "./user";
import { emptyUserProfile } from "@/models/dataModels";

export const useUserInfoStore = defineStore("userInfo", () => {
  const userInfo = ref(emptyUserProfile());
  const loading = ref(false);

  const userStore = useUserStore();

  // 缓存优先：登录/注册时已存了完整用户数据，直接复用
  const hasCache = computed(() => !!userStore.user?.id);

  /**
   * 获取当前用户信息（缓存优先）
   *  - 登录/注册时已存的数据 → 直接使用，不发请求
   *  - 缓存为空（如刷新页面）→ 发请求获取，并同步回 user store
   * @param {boolean} force 为 true 时跳过缓存强制刷新
   */
  async function loadUserInfo(force = false) {
    if (!force && hasCache.value) {
      userInfo.value = { ...emptyUserProfile(), ...userStore.user };
      return userInfo.value;
    }

    loading.value = true;
    try {
      const result = await fetchUserInfoApi();
      userInfo.value = { ...emptyUserProfile(), ...result };
      // 同步回全局 user store，后续导航栏/其他页面都能复用这份缓存
      userStore.updateProfile(result);
    } finally {
      loading.value = false;
    }

    return userInfo.value;
  }

  return {
    userInfo,
    loading,
    hasCache,
    loadUserInfo,
  };
});
