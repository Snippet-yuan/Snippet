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
        <img
          class="follow-avatar"
          src="@/assets/avatar/user-avatar-2.jpg"
          alt="关注头像"
        />
        <div class="follow-meta">
          <div class="follow-name-row">
            <span class="follow-name">林小舟</span>
            <span class="follow-tag">在线</span>
          </div>
          <p class="follow-desc">喜欢收藏优秀设计与前端文章</p>
        </div>
      </div>

      <div class="follow-item">
        <img
          class="follow-avatar"
          src="@/assets/avatar/user-avatar-5.jpg"
          alt="关注头像"
        />
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

<style scoped src="@/style/follows.less"></style>
