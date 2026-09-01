// 导航守卫（beforeEach / afterEach 等逻辑）
import { useUserStore } from "@/stores/user";

export function setupGuards(router) {
  router.beforeEach((to, _, next) => {
    const userStore = useUserStore();

    // 同步 localStorage → 内存，处理用户手动删除 token 的场景
    userStore.syncToken();

    // 已登录用户访问 guestOnly 页面（登录/注册）→ 回首页
    if (userStore.isLoggedIn && to.meta.guestOnly) {
      return next({ name: "Home" });
    }

    // 未登录用户访问受限页面 → 取消导航，弹出登录模态框，登录成功后跳转受限页面
    if (!userStore.isLoggedIn && to.meta.authOnly) {
      userStore.openLoginModal(to.fullPath);
      return next(false);
    }

    // 其余情况正常放行
    next();
  });
}
