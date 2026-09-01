<template>
  <Teleport to="body">
    <Transition name="auth-modal">
      <div
        v-if="userStore.showLoginModal"
        class="auth-modal-overlay"
        @click.self="userStore.closeLoginModal()"
      >
        <Login v-if="userStore.authMode === 'login'" />
        <Register v-else />
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useUserStore } from "@/stores/user";
import Login from "./Login.vue";
import Register from "./Register.vue";

const userStore = useUserStore();
</script>

<style src="@/style/authModal.less"></style>
