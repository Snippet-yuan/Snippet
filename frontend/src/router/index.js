// 创建 router 实例，导出
import { createRouter, createWebHistory } from "vue-router";
import { routes } from "./routes";
import { setupGuards } from "./guards";

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

setupGuards(router);
