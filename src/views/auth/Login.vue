<template>
  <div class="login-page">
    <div class="login-container">
      <div class="header-container">
        <div class="header-icon">
          <img src="" alt="" />
        </div>
        <div class="header-title">
          <h1>欢迎来到 Snippet</h1>
          <p>登录以发现更多专属你的灵感</p>
        </div>
      </div>

      <div class="body-container">
        <form action="" class="login-form">
          <label for="" class="account-label">
            <input type="text" placeholder="电子邮件" />
          </label>
          <label for="" class="password-label">
            <input type="text" placeholder="密码" />
          </label>
          <a href="" class="forget-password-link">忘记密码？</a>
          <button type="submit" class="login-btn">
            <div>登录</div>
          </button>
        </form>
        <p class="or-text">或</p>
        <div class="btn-container">
          <button class="google-btn">
            <div>继续使用 Google 登录</div>
          </button>
          <button class="QR-code-btn">
            <div>使用二维码继续</div>
          </button>
        </div>

        <div class="QR-code">
          <div class="QR-code-text-container">
            <h4>立即登录</h4>
            <p>用手机扫描此二维码立即登录</p>
          </div>
          <div class="QR-code-img-container">
            <div class="refresh-btn">刷新</div>
            <!-- <img src="../../assets/images/QR-code.png" alt="" /> -->
          </div>
        </div>

        <div class="login-footer">
          <div class="new-user-link">
            Snippet新用户？
            <RouterLink to="/register" class="join-link">立即加入</RouterLink>
          </div>
          <div class="privacy-policy-link">
            <a href="" class="privacy-policy-link-text">隐私政策</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const account = ref("");
const password = ref("");

const isFormValid = computed(
  () => account.value.trim() !== "" && password.value.length > 0,
);

function handleLogin() {
  // TODO: 对接登录接口
  console.log("登录", {
    account: account.value.trim(),
    password: password.value,
  });
}
</script>

<style scoped src="@/style/auth.css"></style>
