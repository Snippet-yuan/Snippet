// 创建 router 实例，导出
import { createRouter, createWebHistory } from "vue-router";
import { routes } from "./routes";
import { setupGuards } from "./guards";

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 浏览器前进/后退时使用 savedPosition
    if (savedPosition && (to.meta?.scroll === "keep" || from.meta?.scroll === "keep")) {
      return savedPosition;
    }

    // 导航到首页时，如果 sessionStorage 有保存的滚动位置，不自动滚动到顶部
    if (to.meta?.scroll === "keep") {
      const saved = sessionStorage.getItem("snippet:explore:scrollY");
      if (saved !== null) {
        const y = Number(saved);
        if (!Number.isNaN(y) && y > 0) {
          return { top: y, behavior: "auto" };
        }
      }
    }

    return { top: 0, behavior: "auto" };
  },
});

export default router;

setupGuards(router);
