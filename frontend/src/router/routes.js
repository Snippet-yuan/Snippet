// 纯路由配置（如果路由很多，可以单独拆出来）
export const routes = [
  {
    path: "/",
    component: () => import("@/layouts/MainLayout.vue"),
    children: [
      {
        path: "",
        name: "Home",
        component: () => import("@/views/Explore.vue"),
        meta: { scroll: "keep" },
      },
      {
        path: "user/:userId",
        name: "Profile",
        component: () => import("@/views/Profile.vue"),
        meta: { authOnly: true },
      },
      {
        path: "create",
        name: "Create",
        component: () => import("@/views/Create.vue"),
        meta: { authOnly: true },
      },

      {
        path: "settings",
        name: "Settings",
        component: () => import("@/views/Settings.vue"),
        meta: { authOnly: true },
      },
      {
        path: "message",
        name: "Message",
        component: () => import("@/views/Message.vue"),
        meta: { authOnly: true },
      },
    ],
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/auth/Login.vue"),
    meta: { title: "登录", guestOnly: true },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/auth/Register.vue"),
    meta: { title: "注册", guestOnly: true },
  },
];
