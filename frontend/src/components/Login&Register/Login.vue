<template>
  <div class="login-container">
    <button class="modal-close-btn" @click="userStore.closeLoginModal()">
      ×
    </button>
    <div class="header-container">
      <div class="header-icon">
        <img src="/Snippet-64*64.svg" alt="Snippet logo" />
      </div>
      <div class="header-title">
        <h1>欢迎来到 Snippet</h1>
        <p>登录以发现更多专属你的灵感</p>
      </div>
    </div>

    <div class="body-container">
      <!-- @submit.prevent="handleLogin"：拦截表单默认提交行为，改由 handleLogin 接管 -->
      <form action="" class="login-form" @submit.prevent="handleLogin">
        <!-- 邮箱输入：v-model 双向绑定 email，输入时同步更新 ref -->
        <label for="" class="account-label">
          <input
            v-model="email"
            type="email"
            placeholder="电子邮件"
            autocomplete="email"
          />
          <!-- 字段级错误提示：validate.js 返回 { email: ["邮箱格式不正确"] }，取第一条展示 -->
          <span v-if="fieldErrors.email" class="field-error">
            {{ fieldErrors.email[0] }}
          </span>
        </label>
        <!-- 密码输入：同上，绑定 password -->
        <label for="" class="password-label">
          <input
            v-model="password"
            type="password"
            placeholder="密码"
            autocomplete="current-password"
          />
          <!-- 字段级错误提示：password 校验失败时才渲染 -->
          <span v-if="fieldErrors.password" class="field-error">
            {{ fieldErrors.password[0] }}
          </span>
        </label>
        <a href="" class="forget-password-link">忘记密码？</a>
        <!-- 接口级错误：后端返回的业务错误（邮箱或密码错误等），统一展示在按钮上方 -->
        <p v-if="submitError" class="submit-error">{{ submitError }}</p>
        <!-- 提交按钮：请求中 disable 防重复提交 -->
        <button type="submit" class="login-btn" :disabled="submitting">
          <div>{{ submitting ? "登录中..." : "登录" }}</div>
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
          Snippet新用户？
          <a
            href="javascript:;"
            class="join-link"
            @click="userStore.switchAuthMode('register')"
          >
            立即加入
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
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { validateLoginForm } from "@/utils/validators";

const email = ref("");
const password = ref("");
const fieldErrors = ref({});
const submitError = ref("");
const submitting = ref(false);

const userStore = useUserStore();
const router = useRouter();

async function handleLogin() {
  fieldErrors.value = {};
  submitError.value = "";

  const errors = validateLoginForm({
    email: email.value,
    password: password.value,
  });
  if (errors) {
    // 校验失败（validate.js 返回 { email: [...], password: [...] }），存起来供模板渲染后结束
    fieldErrors.value = errors;
    return;
  }

  // 2. 调接口：走 store -> api -> mock/真实后端
  submitting.value = true;
  try {
    await userStore.login({ email: email.value, password: password.value });
    // 3. 登录成功：记住被拦截页面 → 关弹窗 → 跳回去（没被拦截就留在当前页）
    const pendingRoute = userStore.pendingRoute;
    userStore.closeLoginModal();
    if (pendingRoute) {
      router.push(pendingRoute);
    }
  } catch (e) {
    submitError.value = e.message;
  } finally {
    // 无论成功失败，都恢复按钮可点击
    submitting.value = false;
  }
}
</script>

<style scoped src="@/style/auth.less"></style>
