<template>
  <footer class="footer">
    <div class="footer-main">
      <RouterLink to="/" class="brand">
        <!-- <img src="/Snippet-64*64.svg" alt="Snippet" class="brand-logo" /> -->
        <img src="/Snippet-text.svg" class="brand-logo" alt="" />
      </RouterLink>

      <nav class="footer-nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/create">创作</RouterLink>
        <RouterLink to="/message">消息</RouterLink>
        <RouterLink to="/settings">设置</RouterLink>
      </nav>

      <div class="footer-social">
        <a href="#" aria-label="GitHub">
          <PhGithubLogo :size="20" />
        </a>
        <a href="#" aria-label="Twitter">
          <PhTwitterLogo :size="20" />
        </a>
        <a href="#" aria-label="Email">
          <PhEnvelopeSimple :size="20" />
        </a>
      </div>
    </div>

    <div class="footer-bottom">
      <span>&copy; {{ currentYear }} Snippet. All rights reserved.</span>
    </div>
  </footer>
</template>

<script setup>
import {
  PhGithubLogo,
  PhTwitterLogo,
  PhEnvelopeSimple,
} from "@phosphor-icons/vue";

const currentYear = new Date().getFullYear();
</script>

<style scoped src="@/style/footer.css"></style>
