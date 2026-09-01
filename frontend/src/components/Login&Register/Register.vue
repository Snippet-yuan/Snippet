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
      <form action="" class="login-form">
        <label for="" class="email-label">
          <input type="text" placeholder="电子邮件" />
        </label>
        <label for="" class="password-label">
          <input type="password" placeholder="密码" />
        </label>
        <label for="" class="confirm-password-label">
          <input type="password" placeholder="确认密码" />
        </label>
        <button type="submit" class="login-btn">
          <div>注 册</div>
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
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
</script>

<style scoped src="@/style/auth.less"></style>