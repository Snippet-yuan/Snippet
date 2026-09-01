import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import "./style/global.less";
import "./style/variables.less";

if (import.meta.env.DEV) {
  await import("@/mock");
}

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount("#app");
