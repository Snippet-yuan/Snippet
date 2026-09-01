<template>
  <div class="login-container">
    <button class="modal-close-btn" @click="userStore.closeLoginModal()">×</button>
    <div class="header-container">
      <div class="header-icon">
        <img src="/Snippet-64*64.svg" alt="Snippet logo" />
      </div>
      <div class="header-title">
        <h1>创建账号</h1>
        <p>加入 Snippet，发现更多专属你的灵感</p>
      </div>
    </div>

    <div class="body-container">
      <form action="" class="login-form" @submit.prevent="handleRegister">
        <label for="" class="email-label">
          <input v-model="nickname" type="text" placeholder="昵称" />
          <span v-if="fieldErrors.nickname" class="field-error">
            {{ fieldErrors.nickname[0] }}
          </span>
        </label>
        <label for="" class="email-label">
          <input v-model="email" type="text" placeholder="电子邮件" />
          <span v-if="fieldErrors.email" class="field-error">
            {{ fieldErrors.email[0] }}
          </span>
        </label>
        <label for="" class="password-label">
          <input v-model="password" type="password" placeholder="密码" />
          <span v-if="fieldErrors.password" class="field-error">
            {{ fieldErrors.password[0] }}
          </span>
        </label>
        <label for="" class="confirm-password-label">
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="确认密码"
          />
          <span v-if="fieldErrors.confirmPassword" class="field-error">
            {{ fieldErrors.confirmPassword[0] }}
          </span>
        </label>
        <p v-if="submitError" class="submit-error">{{ submitError }}</p>
        <button type="submit" class="login-btn" :disabled="submitting">
          <div>{{ submitting ? "注册中..." : "注 册" }}</div>
        </button>
      </form>

      <p class="or-text">或</p>
      <div class="btn-container">
        <button class="google-btn">
          <div>继续使用 Google 登录</div>
        </button>
      </div>

      <div class="login-footer">
        <div class="new-user-link">
          已有账号？
          <a
            href="javascript:;"
            class="join-link"
            @click="userStore.switchAuthMode('login')"
          >
            立即登录
          </a>
        </div>
        <div class="privacy-policy-link">
          <a href="" class="privacy-policy-link-text">隐私政策</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useUserStore } from "@/stores/user";
import { validateRegisterForm } from "@/utils/validators";

const userStore = useUserStore();

const nickname = ref("");
const email = ref("");
const password = ref("");
const confirmPassword = ref("");
const fieldErrors = ref({});
const submitError = ref("");
const submitting = ref(false);

async function handleRegister() {
  fieldErrors.value = {};
  submitError.value = "";

  const errors = validateRegisterForm({
    nickname: nickname.value,
    email: email.value,
    password: password.value,
    confirmPassword: confirmPassword.value,
  });

  if (errors) {
    fieldErrors.value = errors;
    return;
  }

  submitting.value = true;
  try {
    await userStore.register({
      email: email.value,
      password: password.value,
      nickname: nickname.value,
    });
    userStore.closeLoginModal();
  } catch (error) {
    submitError.value = error.message || "注册失败，请稍后重试";
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped src="@/style/auth.less"></style>