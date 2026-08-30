<template>
  <div class="login-page">
    <div class="login-container">
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

<!-- 下面的 <script setup> 是这份表单的“控制层”：收集输入 → 校验 → 发请求 → 跳转/报错 -->
<script setup>
// ref：响应式变量，值变化会自动触发模板更新
import { ref } from "vue";
// useRouter：编程式导航，登录成功后跳回首页用
import { useRouter } from "vue-router";
// 引入 Pinia store：登录状态、token 持久化放在这里
import { useUserStore } from "@/stores/user";
// 前端校验函数：validate.js 的薄封装，返回 undefined（通过）或 { 字段: [错误文案] }（失败）
import { validateLoginForm } from "@/utils/validators";

// 表单数据：用户输入的邮箱和密码
const email = ref("");
const password = ref("");
// 字段级错误：按字段分组，便于在对应输入框下方显示
// 形如 { email: ["邮箱格式不正确"], password: ["密码至少 6 位"] }
const fieldErrors = ref({});
// 接口级错误：后端业务错误，统一展示在按钮上方，与字段错误分开
const submitError = ref("");
// 提交状态：请求期间设为 true，按钮 disable 并显示"登录中..."
const submitting = ref(false);

// Pinia store 实例：管 token、user 的存取和 persist
const userStore = useUserStore();
// 路由实例：登录成功后用 router.push 跳转
const router = useRouter();

// 提交 handler：@submit.prevent="handleLogin" 绑定到这里
async function handleLogin() {
  // 清空上一轮的错误展示（否则新提交时旧错误还在）
  fieldErrors.value = {};
  submitError.value = "";

  // 1. 前端校验：本地判空/格式，不满足就不发请求
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
    // 3. 登录成功：跳回首页
    router.push("/");
  } catch (e) {
    // 4. 接口失败：把后端返回的 message（如"邮箱或密码错误"）展示在页面上
    submitError.value = e.message;
  } finally {
    // 无论成功失败，都恢复按钮可点击
    submitting.value = false;
  }
}
</script>

<style src="@/style/auth.less"></style>
