import { defineStore } from "pinia";
import { login as loginApi } from "@/api/auth";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: localStorage.getItem("snippet_token") || "",
    user: null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
  },

  actions: {
    async login({ email, password }) {
      const { token, user } = await loginApi({ email, password });

      console.log(token, user);

      this.token = token;
      this.user = user;
      localStorage.setItem("snippet_token", token);
    },

    logout() {
      this.token = "";
      this.user = null;
      localStorage.removeItem("snippet_token");
    },
  },
});
