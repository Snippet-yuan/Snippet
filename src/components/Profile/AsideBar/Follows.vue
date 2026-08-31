<template>
  <section class="aside-card follows-card">
    <div class="aside-card-header">
      <div>
        <p class="aside-card-kicker">Social</p>
        <h3 class="aside-card-title">关注的人</h3>
      </div>
      <span class="aside-card-badge badge-soft">24</span>
    </div>

    <div class="follow-list">
      <div class="follow-item">
        <img class="follow-avatar" src="@/assets/avatar/user-avatar-2.jpg" alt="关注头像" />
        <div class="follow-meta">
          <div class="follow-name-row">
            <span class="follow-name">林小舟</span>
            <span class="follow-tag">在线</span>
          </div>
          <p class="follow-desc">喜欢收藏优秀设计与前端文章</p>
        </div>
      </div>

      <div class="follow-item">
        <img class="follow-avatar" src="@/assets/avatar/user-avatar-5.jpg" alt="关注头像" />
        <div class="follow-meta">
          <div class="follow-name-row">
            <span class="follow-name">阿柒</span>
            <span class="follow-tag tag-muted">2h</span>
          </div>
          <p class="follow-desc">正在发布新的写作系列</p>
        </div>
      </div>

      <div class="follow-item follow-item-compact">
        <div class="follow-mini-avatar">+4</div>
        <div class="follow-meta">
          <div class="follow-name-row">
            <span class="follow-name">更多关注</span>
            <span class="follow-tag tag-blue">查看全部</span>
          </div>
          <p class="follow-desc">还有 4 位关注者未展示</p>
        </div>
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

.badge-soft {
  background: linear-gradient(135deg, #ff7a59 0%, #ff9a7c 100%);
  box-shadow: 0 8px 18px rgba(255, 122, 89, 0.22);
}

.follow-list {
  display: grid;
  gap: 12px;
}

.follow-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #eef2f8;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.follow-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(31, 35, 41, 0.08);
}

.follow-avatar,
.follow-mini-avatar {
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 14px;
  object-fit: cover;
  background: #eef2f8;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.5);
}

.follow-mini-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #5b7cfa;
  background: linear-gradient(135deg, #edf2ff 0%, #f8fbff 100%);
}

.follow-meta {
  min-width: 0;
  flex: 1;
}

.follow-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.follow-name {
  font-size: 14px;
  font-weight: 700;
  color: #1f2329;
}

.follow-tag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #2e7d32;
  background: #eaf7ee;
}

.tag-muted {
  color: #8a919f;
  background: #f3f5f7;
}

.tag-blue {
  color: #5b7cfa;
  background: #eef2ff;
}

.follow-desc {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #8a919f;
}

.follow-item-compact {
  align-items: center;
}
</style>
