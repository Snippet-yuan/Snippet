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

<style scoped>
.aside-card {
  width: 420px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10px 30px rgba(31, 35, 41, 0.08);
  border: 1px solid rgba(91, 124, 250, 0.08);
}

.aside-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.aside-card-kicker {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #8a919f;
}

.aside-card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
}

.aside-card-badge {
  min-width: 34px;
  height: 34px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: linear-gradient(135deg, #5b7cfa 0%, #7f8cff 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(91, 124, 250, 0.28);
}

.followers-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eef2ff 0%, #f8fbff 100%);
  margin-bottom: 14px;
}

.followers-hero-number {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  color: #1f2329;
}

.followers-hero-text {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.followers-hero-right {
  position: relative;
  width: 104px;
  height: 64px;
}

.mini-fan {
  position: absolute;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 6px 16px rgba(31, 35, 41, 0.12);
  background-size: cover;
  background-position: center;
}

.mini-fan-1 {
  left: 0;
  top: 10px;
  background-image: url("@/assets/avatar/user-avatar-3.jpg");
}

.mini-fan-2 {
  left: 30px;
  top: 0;
  background-image: url("@/assets/avatar/user-avatar-6.jpg");
}

.mini-fan-3 {
  left: 60px;
  top: 12px;
  background-image: url("@/assets/avatar/user-avatar-8.jpg");
}

.followers-list {
  display: grid;
  gap: 10px;
}

.followers-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #eef2f8;
}

.followers-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot-green {
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12);
}

.dot-blue {
  background: #5b7cfa;
  box-shadow: 0 0 0 4px rgba(91, 124, 250, 0.12);
}

.dot-orange {
  background: #ff8a65;
  box-shadow: 0 0 0 4px rgba(255, 138, 101, 0.12);
}

.followers-label {
  font-size: 13px;
  font-weight: 600;
  color: #4e5969;
}

.followers-value {
  font-size: 13px;
  font-weight: 700;
  color: #1f2329;
}
</style>
