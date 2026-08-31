<template>
  <div class="conversation-list">
    <div
      v-for="conversation in conversationList"
      :key="conversation.id"
      class="conversation-list-item"
      :class="{ 'is-active': conversation.id === 1 }"
      @click="$emit('select-conversation', conversation.name)"
    >
      <div class="conversation-list-item-avatar">
        <img
          class="conversation-list-item-avatar-img"
          :src="conversation.avatar"
          :alt="`${conversation.name}的头像`"
        />
        <span v-if="conversation.online" class="online-indicator"></span>
      </div>
      <div class="conversation-list-item-container">
        <div class="conversation-list-item-heading">
          <div class="conversation-list-item-name">{{ conversation.name }}</div>
          <time>{{ conversation.time }}</time>
        </div>
        <div class="conversation-list-item-preview">
          <div class="conversation-list-item-message">
            {{ conversation.message }}
          </div>
          <span v-if="conversation.unread" class="unread-badge">
            {{ conversation.unread }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import avatar2 from "@/assets/avatar/user-avatar-2.jpg";
import avatar3 from "@/assets/avatar/user-avatar-3.jpg";
import avatar4 from "@/assets/avatar/user-avatar-4.jpg";
import avatar5 from "@/assets/avatar/user-avatar-5.jpg";
import avatar6 from "@/assets/avatar/user-avatar-6.jpg";
import avatar7 from "@/assets/avatar/user-avatar-7.jpg";
import avatar8 from "@/assets/avatar/user-avatar-8.jpg";

defineEmits(["select-conversation"]);

const conversationList = [
  {
    id: 1,
    name: "林小舟",
    message: "那个组件的交互我又优化了一版，给你看看？",
    time: "10:44",
    avatar: avatar2,
    online: true,
  },
  {
    id: 2,
    name: "阿柒 · 产品设计",
    message: "我把首页的视觉稿和交互说明都整理到文档里了。",
    time: "09:28",
    avatar: avatar5,
    online: true,
    unread: 3,
  },
  {
    id: 3,
    name: "产品讨论组",
    message: "陈默：关于下周的发布计划，大家还有什么建议吗？",
    time: "昨天",
    avatar: avatar6,
    unread: 12,
  },
  {
    id: 4,
    name: "陈默 · 体验设计师",
    message: "下午三点方便吗？我们一起过一下新版本的需求细节。",
    time: "昨天",
    avatar: avatar7,
    online: true,
  },
  {
    id: 5,
    name: "灵感交换站",
    message: "你分享的那篇文章很有启发，尤其是关于留白的部分。",
    time: "周一",
    avatar: avatar8,
  },
  {
    id: 6,
    name: "周宁 · 前端开发",
    message: "接口已经联调完成，剩下的状态处理我今晚补上。",
    time: "周日",
    avatar: avatar3,
    online: true,
  },
  {
    id: 7,
    name: "小满",
    message: "周末去看展吗？听说新开的摄影展主题很有意思。",
    time: "周六",
    avatar: avatar4,
  },
  {
    id: 8,
    name: "创作者交流小组",
    message: "本周的分享主题是组件设计，欢迎大家带作品来交流。",
    time: "周五",
    avatar: avatar2,
    unread: 5,
  },
  {
    id: 9,
    name: "许知远 · 独立开发者",
    message: "我刚把项目的演示地址发给你，欢迎帮忙看看整体流程。",
    time: "周四",
    avatar: avatar5,
    online: true,
  },
  {
    id: 10,
    name: "夏末读书会",
    message: "下次活动我们聊聊设计系统在团队协作中的实际应用吧。",
    time: "周四",
    avatar: avatar6,
    unread: 2,
  },
  {
    id: 11,
    name: "苏晚晴",
    message: "你上次推荐的播客我听完了，里面关于创作的观点很精彩。",
    time: "周三",
    avatar: avatar7,
  },
  {
    id: 12,
    name: "技术方案评审群",
    message: "新的接口文档已经更新，请大家在明天中午之前完成确认。",
    time: "周三",
    avatar: avatar8,
    unread: 8,
  },
  {
    id: 13,
    name: "顾言 · 内容运营",
    message: "这周的专题文章数据不错，我们可以继续保持这个选题方向。",
    time: "周二",
    avatar: avatar3,
    online: true,
  },
  {
    id: 14,
    name: "远方的朋友",
    message: "最近天气变凉了，记得照顾好自己，有时间一起喝杯咖啡。",
    time: "周二",
    avatar: avatar4,
  },
  {
    id: 15,
    name: "视觉探索计划",
    message: "我整理了几组新的配色方案，晚点放到共享文件夹里。",
    time: "周一",
    avatar: avatar2,
    unread: 4,
  },
  {
    id: 16,
    name: "唐可欣 · 用户研究",
    message: "用户访谈记录我已经归纳好了，核心问题比预期更集中。",
    time: "上周日",
    avatar: avatar5,
    online: true,
  },
  {
    id: 17,
    name: "夜航船",
    message: "如果你喜欢慢节奏的城市故事，可以看看这期新出的通讯。",
    time: "上周日",
    avatar: avatar6,
  },
  {
    id: 18,
    name: "项目协作空间",
    message: "部署环境已经准备完毕，今天可以开始第一轮完整测试。",
    time: "上周六",
    avatar: avatar7,
    unread: 6,
  },
  {
    id: 19,
    name: "沈言 · 后端工程师",
    message: "数据库迁移脚本已经跑通，回滚方案也一并补充好了。",
    time: "上周六",
    avatar: avatar8,
    online: true,
  },
  {
    id: 20,
    name: "周末摄影小组",
    message: "有人想参加这周日下午的街拍活动吗？路线已经规划好了。",
    time: "上周五",
    avatar: avatar3,
  },
  {
    id: 21,
    name: "李想",
    message: "新的动效看起来很自然，尤其是页面切换时的过渡效果。",
    time: "上周四",
    avatar: avatar4,
  },
  {
    id: 22,
    name: "开源项目维护者",
    message: "感谢你的贡献，我已经合并了这次修复并更新了版本说明。",
    time: "上周三",
    avatar: avatar2,
    online: true,
  },
  {
    id: 23,
    name: "三人产品小分队",
    message: "关于登录流程，我们最终决定先采用更轻量的验证方式。",
    time: "上周二",
    avatar: avatar5,
    unread: 7,
  },
  {
    id: 24,
    name: "叶青 · 动效设计",
    message: "我把按钮和弹窗的动效参数重新整理了一份，方便直接复用。",
    time: "上周一",
    avatar: avatar6,
  },
  {
    id: 25,
    name: "城市漫游指南",
    message: "这家新开的咖啡馆环境很不错，下次路过附近可以去试试。",
    time: "8月28日",
    avatar: avatar7,
  },
  {
    id: 26,
    name: "陈一鸣 · 软件工程",
    message: "代码评审里的建议我都处理好了，麻烦再帮我看一眼。",
    time: "8月27日",
    avatar: avatar8,
    online: true,
  },
  {
    id: 27,
    name: "播客听友群",
    message: "本期节目已经上线，大家可以在群里分享自己的听后感。",
    time: "8月26日",
    avatar: avatar3,
    unread: 9,
  },
  {
    id: 28,
    name: "莫奈花园",
    message: "周五晚上有一场小型分享会，主题是如何建立个人知识库。",
    time: "8月25日",
    avatar: avatar4,
  },
  {
    id: 29,
    name: "林深 · 摄影师",
    message: "我选了几张上次活动的照片，原图稍后通过云盘发给你。",
    time: "8月24日",
    avatar: avatar2,
    online: true,
  },
  {
    id: 30,
    name: "Snippet 社区通知",
    message: "欢迎加入 Snippet，期待在这里看到你的作品和有趣想法。",
    time: "8月23日",
    avatar: avatar5,
  },
];
</script>

<style scoped>
.conversation-list {
  flex: 0 0 414px;
  height: calc(100vh - 72px);
  overflow: auto;
  background: #fff;
}

.conversation-list-item {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 82px;
  margin: 0 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #edf0f5;
  border-radius: 14px;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
}

.conversation-list-item:hover {
  background: #f6f8ff;
  transform: translateX(2px);
}

.conversation-list-item.is-active {
  background: #eef2ff;
}

.conversation-list-item-avatar {
  position: relative;
  flex: 0 0 auto;
}

.conversation-list-item-avatar-img {
  display: block;
  width: 52px;
  height: 52px;
  object-fit: cover;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.12);
}

.online-indicator {
  position: absolute;
  right: 0;
  bottom: 1px;
  width: 11px;
  height: 11px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #42c98a;
}

.conversation-list-item-container {
  min-width: 0;
  flex: 1;
  margin-left: 12px;
  padding-right: 4px;
}

.conversation-list-item-heading,
.conversation-list-item-preview {
  display: flex;
  align-items: center;
}

.conversation-list-item-heading {
  justify-content: space-between;
  gap: 10px;
}

.conversation-list-item-preview {
  min-width: 0;
  gap: 8px;
}

.conversation-list-item-name {
  overflow: hidden;
  color: #202938;
  font-size: 15px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-list-item-heading time {
  min-width: 38px;
  color: #a1aaba;
  font-size: 11px;
  font-weight: 400;
  line-height: 1.2;
  text-align: right;
  white-space: nowrap;
}

.conversation-list-item-message {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  color: #818c9d;
  font-size: 13px;
  font-weight: 400;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  flex: 0 0 26px;
  height: 20px;
  margin-top: 6px;
  padding: 0 3px;
  border-radius: 999px;
  color: #fff;
  background: #ff1c1c;
  font-size: 10px;
  font-weight: 800;
  line-height: 20px;
  text-align: center;
}
</style>
