<template>
  <div class="settings-page">
    <!-- 顶部用户信息栏 -->
    <header class="settings-header">
      <div class="user-info">
        <img
          class="avatar"
          src="https://avatars.githubusercontent.com/u/1?v=4"
          alt="avatar"
        />
        <div class="user-meta">
          <h1 class="user-name">桥元涛（桥元涛）</h1>
          <p class="user-desc">您的个人帐户</p>
        </div>
      </div>

      <button class="switch-context-btn">
        <PhArrowsClockwise :size="16" />
        切换设置上下文
        <PhCaretDown :size="14" />
      </button>
    </header>

    <div class="settings-body">
      <!-- 左侧导航 -->
      <aside class="settings-sidebar">
        <nav class="sidebar-nav">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="nav-item"
            :class="{ active: activeMenu === item.key }"
            @click="activeMenu = item.key"
          >
            <component :is="item.icon" :size="16" class="nav-icon" />
            <span>{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="settings-content">
        <!-- 简介 -->
        <section v-if="activeMenu === 'profile'" class="content-section">
          <h2 class="section-title">公众简介</h2>
          <div class="divider"></div>

          <div class="form-layout">
            <div class="form-left">
              <div class="form-group">
                <label class="form-label">名字</label>
                <input
                  v-model="form.name"
                  type="text"
                  class="form-input"
                  placeholder="请输入名字"
                />
                <p class="form-hint">
                  你的名字可能会出现在平台周围，在那里你做出贡献或被提及。你可以随时删除它。
                </p>
              </div>

              <div class="form-group">
                <label class="form-label">个人描述</label>
                <textarea
                  v-model="form.bio"
                  class="form-textarea"
                  rows="4"
                  placeholder="告诉我们一些关于你自己的事情"
                ></textarea>
                <p class="form-hint">
                  您可以 @mention 其他用户和组织来链接到他们。
                </p>
              </div>

              <div class="form-group">
                <label class="form-label">社交账户</label>
                <div class="social-inputs">
                  <div class="social-item">
                    <PhLink :size="16" class="social-icon" />
                    <input
                      v-model="form.social1"
                      type="text"
                      class="form-input"
                      placeholder="链接到社交资料1"
                    />
                  </div>
                  <div class="social-item">
                    <PhLink :size="16" class="social-icon" />
                    <input
                      v-model="form.social2"
                      type="text"
                      class="form-input"
                      placeholder="链接到社交资料2"
                    />
                  </div>
                  <div class="social-item">
                    <PhLink :size="16" class="social-icon" />
                    <input
                      v-model="form.social3"
                      type="text"
                      class="form-input"
                      placeholder="链接到社交个人资料3"
                    />
                  </div>
                </div>
              </div>
            </div>

            <div class="form-right">
              <label class="form-label">个人资料图片</label>
              <div class="avatar-upload">
                <img
                  class="profile-avatar"
                  src="https://avatars.githubusercontent.com/u/1?v=4"
                  alt="profile"
                />
                <button class="edit-avatar-btn">
                  <PhPencilSimple :size="14" />
                  编辑
                </button>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button class="btn-primary">更新资料</button>
          </div>
        </section>

        <!-- 外观 -->
        <section
          v-else-if="activeMenu === 'appearance'"
          class="content-section"
        >
          <h2 class="section-title">外观</h2>
          <div class="divider"></div>

          <div class="form-group">
            <label class="form-label">主题偏好</label>
            <div class="theme-options">
              <label class="theme-option">
                <input type="radio" v-model="form.theme" value="light" />
                <span>浅色</span>
              </label>
              <label class="theme-option">
                <input type="radio" v-model="form.theme" value="dark" />
                <span>深色</span>
              </label>
              <label class="theme-option">
                <input type="radio" v-model="form.theme" value="system" />
                <span>跟随系统</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">强调色</label>
            <select v-model="form.accentColor" class="form-select">
              <option value="blue">蓝色</option>
              <option value="purple">紫色</option>
              <option value="green">绿色</option>
              <option value="orange">橙色</option>
            </select>
          </div>
        </section>

        <!-- 通知 -->
        <section
          v-else-if="activeMenu === 'notifications'"
          class="content-section"
        >
          <h2 class="section-title">通知</h2>
          <div class="divider"></div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.notifyEmail" />
              <span>通过电子邮件接收通知</span>
            </label>
          </div>
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.notifyWeb" />
              <span>在网页上显示通知</span>
            </label>
          </div>
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.notifyMention" />
              <span>有人 @我时通知我</span>
            </label>
          </div>
        </section>

        <!-- 电子邮件 -->
        <section v-else-if="activeMenu === 'emails'" class="content-section">
          <h2 class="section-title">电子邮件</h2>
          <div class="divider"></div>

          <div class="form-group">
            <label class="form-label">添加电子邮件地址</label>
            <div class="input-with-btn">
              <input
                v-model="form.newEmail"
                type="email"
                class="form-input"
                placeholder="输入新的电子邮件"
              />
              <button class="btn-secondary">添加</button>
            </div>
          </div>

          <div class="email-list">
            <div class="email-item">
              <span>example@email.com</span>
              <span class="badge">主要</span>
              <span class="badge verified">已验证</span>
            </div>
          </div>
        </section>

        <!-- 密码和身份验证（已合并账户内容） -->
        <section v-else-if="activeMenu === 'password'" class="content-section">
          <h2 class="section-title">密码和身份验证</h2>
          <div class="divider"></div>

          <!-- 更改用户名 -->
          <div class="form-group">
            <label class="form-label">更改用户名</label>
            <div class="input-with-btn">
              <input v-model="form.username" type="text" class="form-input" />
              <button class="btn-secondary">更改用户名</button>
            </div>
          </div>

          <!-- 更改密码 -->
          <div class="form-group">
            <label class="form-label">更改密码</label>
            <input
              v-model="form.oldPassword"
              type="password"
              class="form-input"
              placeholder="当前密码"
            />
            <input
              v-model="form.newPassword"
              type="password"
              class="form-input mt-8"
              placeholder="新密码"
            />
            <input
              v-model="form.confirmPassword"
              type="password"
              class="form-input mt-8"
              placeholder="确认新密码"
            />
            <button class="btn-primary mt-12">更新密码</button>
          </div>

          <!-- 危险区域 -->
          <div class="danger-zone">
            <h3 class="danger-title">危险区域</h3>
            <p class="form-hint">删除账户后，所有数据将无法恢复。</p>
            <button class="btn-danger">删除账户</button>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import {
  PhUser,
  PhPalette,
  PhBell,
  PhEnvelope,
  PhLock,
  PhArrowsClockwise,
  PhCaretDown,
  PhLink,
  PhPencilSimple,
} from "@phosphor-icons/vue";

const activeMenu = ref("profile");

const menuItems = [
  { key: "profile", label: "简介", icon: PhUser },
  { key: "appearance", label: "外观", icon: PhPalette },
  { key: "notifications", label: "通知", icon: PhBell },
  { key: "emails", label: "电子邮件", icon: PhEnvelope },
  { key: "password", label: "密码和身份验证", icon: PhLock },
];

const form = reactive({
  name: "",
  bio: "",
  social1: "",
  social2: "",
  social3: "",
  username: "qiaoyuantao",
  theme: "system",
  accentColor: "blue",
  notifyEmail: true,
  notifyWeb: true,
  notifyMention: true,
  newEmail: "",
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #fff;
  color: #1f2328;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans", Helvetica,
    Arial, sans-serif;
}

/* 顶部 */
.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  border-bottom: 1px solid #d0d7de;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.user-desc {
  margin: 2px 0 0;
  font-size: 14px;
  color: #656d76;
}

.switch-context-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  font-size: 14px;
  color: #24292f;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
}

.switch-context-btn:hover {
  background: #f3f4f6;
}

/* 主体 */
.settings-body {
  display: flex;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 32px;
  gap: 24px;
}

/* 左侧导航 */
.settings-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  font-size: 14px;
  color: #24292f;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.nav-item:hover {
  background: #f6f8fa;
}

.nav-item.active {
  background: #ddf4ff;
  color: #0969da;
  font-weight: 600;
}

.nav-icon {
  flex-shrink: 0;
  opacity: 0.8;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  min-width: 0;
  max-width: 860px;
}

.section-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 400;
}

.divider {
  height: 1px;
  background: #d0d7de;
  margin-bottom: 24px;
}

/* 表单 */
.form-layout {
  display: flex;
  gap: 40px;
}

.form-left {
  flex: 1;
  min-width: 0;
}

.form-right {
  width: 200px;
  flex-shrink: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 5px 12px;
  font-size: 14px;
  line-height: 20px;
  color: #1f2328;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
  transition:
    border-color 0.15s,
    box-shadow 0.15s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.3);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #656d76;
  line-height: 1.5;
}

.mt-8 {
  margin-top: 8px;
}

.mt-12 {
  margin-top: 12px;
}

/* 社交 */
.social-inputs {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.social-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.social-icon {
  color: #656d76;
  flex-shrink: 0;
}

/* 头像 */
.avatar-upload {
  position: relative;
  display: inline-block;
}

.profile-avatar {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #d0d7de;
}

.edit-avatar-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.edit-avatar-btn:hover {
  background: #f6f8fa;
}

/* 按钮 */
.form-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #d0d7de;
}

.btn-primary {
  padding: 5px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: #1f883d;
  border: 1px solid rgba(31, 35, 40, 0.15);
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #1a7f37;
}

.btn-secondary {
  padding: 5px 16px;
  font-size: 14px;
  color: #24292f;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #f3f4f6;
}

.btn-danger {
  padding: 5px 16px;
  font-size: 14px;
  color: #cf222e;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
}

.btn-danger:hover {
  background: #ffebe9;
  border-color: #ff8182;
}

.input-with-btn {
  display: flex;
  gap: 8px;
}

.input-with-btn .form-input {
  flex: 1;
}

/* 主题 */
.theme-options {
  display: flex;
  gap: 16px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  cursor: pointer;
}

/* 复选框 */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

/* 危险区域 */
.danger-zone {
  margin-top: 32px;
  padding: 16px;
  border: 1px solid #ff8182;
  border-radius: 6px;
}

.danger-title {
  margin: 0 0 8px;
  font-size: 15px;
  color: #cf222e;
}

/* 邮件 */
.email-list {
  margin-top: 16px;
}

.email-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
}

.badge {
  padding: 2px 8px;
  font-size: 12px;
  background: #ddf4ff;
  color: #0969da;
  border-radius: 2em;
}

.badge.verified {
  background: #dafbe1;
  color: #1a7f37;
}

/* 响应式 */
@media (max-width: 900px) {
  .settings-body {
    flex-direction: column;
  }

  .settings-sidebar {
    width: 100%;
  }

  .form-layout {
    flex-direction: column;
  }

  .form-right {
    width: 100%;
  }
}
</style>
