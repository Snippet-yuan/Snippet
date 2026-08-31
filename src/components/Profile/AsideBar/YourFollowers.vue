<template>
  <section class="aside-card followers-card">
    <div class="aside-card-header">
      <div>
        <p class="aside-card-kicker">Community</p>
        <h3 class="aside-card-title">你的粉丝</h3>
      </div>
      <span class="aside-card-badge">128</span>
    </div>

    <div class="followers-hero">
      <div class="followers-hero-left">
        <div class="followers-hero-number">128</div>
        <div class="followers-hero-text">粉丝正在持续增长</div>
      </div>
      <div class="followers-hero-right">
        <div class="mini-fan mini-fan-1"></div>
        <div class="mini-fan mini-fan-2"></div>
        <div class="mini-fan mini-fan-3"></div>
      </div>
    </div>

    <div class="followers-list">
      <div class="followers-row">
        <div class="followers-info">
          <span class="dot dot-green"></span>
          <span class="followers-label">今天新增</span>
        </div>
        <span class="followers-value">+12</span>
      </div>

      <div class="followers-row">
        <div class="followers-info">
          <span class="dot dot-blue"></span>
          <span class="followers-label">最近互动</span>
        </div>
        <span class="followers-value">86%</span>
      </div>

      <div class="followers-row">
        <div class="followers-info">
          <span class="dot dot-orange"></span>
          <span class="followers-label">活跃粉丝</span>
        </div>
        <span class="followers-value">41</span>
      </div>
    </div>
  </section>
</template>

<style scoped src="@/style/yourFollowers.less"></style>
