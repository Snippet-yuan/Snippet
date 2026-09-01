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

<style scoped>
.footer {
  width: 100%;
  padding: 40px 24px 24px;
  background-color: #fff;
  border-top: 1px solid #f2f2f2;
}

.footer-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1280px;
  margin-right: 56px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #111;
}

.brand-logo {
  width: 32px;
  height: 32px;
}

.brand-logo {
  width: 144px;
  height: 56px;
  object-fit: cover;
}

.footer-nav {
  display: flex;
  align-items: center;
  gap: 32px;
}

.footer-nav a {
  font-size: 14px;
  font-weight: 400;
  color: #666;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-nav a:hover {
  color: #111;
}

.footer-social {
  display: flex;
  align-items: center;
  gap: 16px;
}

.footer-social a {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: #666;
  background-color: #fafafa;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.footer-social a:hover {
  color: #111;
  background-color: #f2f2f2;
}

.footer-bottom {
  display: flex;
  justify-content: center;
  max-width: 1200px;
  margin: 32px auto 0;
  padding-top: 20px;
  border-top: 1px solid #f2f2f2;
  font-size: 12px;
  color: #999;
}

@media (max-width: 640px) {
  .footer-main {
    flex-direction: column;
    gap: 24px;
  }

  .footer-nav {
    gap: 20px;
  }

  .footer-bottom {
    margin-top: 24px;
  }
}
</style>
