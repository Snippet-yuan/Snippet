<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="profile-avatar">小秋</div>
      <div class="profile-info">
        <h1>小秋</h1>
        <p class="profile-bio">热爱分享代码的前端工程师，专注 Vue 与前端工程化</p>
        <div class="profile-stats">
          <div class="stat">
            <span class="stat-num">128</span>
            <span class="stat-label">代码片段</span>
          </div>
          <div class="stat">
            <span class="stat-num">1.2k</span>
            <span class="stat-label">关注者</span>
          </div>
          <div class="stat">
            <span class="stat-num">56</span>
            <span class="stat-label">关注中</span>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-card">
      <h2>个人简介</h2>
      <p>
        你好，我是小秋。平时喜欢把工作中遇到的常用代码片段整理成笔记分享出来，
        涵盖 Vue、JavaScript、CSS、Git 等日常开发内容。这里记录了我的全部作品。
      </p>
    </div>

    <div class="profile-card">
      <h2>技能标签</h2>
      <div class="tags">
        <span class="tag">Vue</span>
        <span class="tag">JavaScript</span>
        <span class="tag">TypeScript</span>
        <span class="tag">CSS</span>
        <span class="tag">Vite</span>
        <span class="tag">Git</span>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped src="@/style/profile.css"></style>