<template>
  <div class="side-action-bar">
    <!-- 用户头像 + 关注 -->
    <div class="action-item avatar-wrap">
      <div class="avatar-box">
        <img class="avatar-img" :src="ownerAvatar" alt="avatar" />
        <div class="follow-badge">
          <PhPlus weight="bold" :size="10" color="#fff" />
        </div>
      </div>
      <span class="action-label">{{ ownerNickname }}</span>
    </div>

    <!-- 点赞 -->
    <div class="action-item" :class="{ liked: liked }" @click.stop="toggleLike">
      <div class="icon-circle">
        <PhHeart weight="fill" :size="28" class="action-icon" />
      </div>
      <span class="action-count">{{ counters.likeCount }}</span>
    </div>

    <!-- 评论 -->
    <div
      class="action-item"
      :class="{ commented: commented }"
      @click.stop="toggleComment"
    >
      <div class="icon-circle">
        <PhChatCircleText weight="fill" :size="28" class="action-icon" />
      </div>
      <span class="action-count">{{ counters.commentCount }}</span>
    </div>

    <!-- 收藏 -->
    <div
      class="action-item"
      :class="{ collected: favorited }"
      @click.stop="toggleCollect"
    >
      <div class="icon-circle">
        <PhBookmarks weight="fill" :size="26" class="action-icon" />
      </div>
      <span class="action-count">{{ counters.favoriteCount }}</span>
    </div>

    <!-- 分享 -->
    <div
      class="action-item"
      :class="{ shared: shared }"
      @click.stop="toggleShare"
    >
      <div class="icon-circle">
        <PhShareFat weight="fill" :size="26" class="action-icon" />
      </div>
      <span class="action-count">{{ counters.shareCount }}</span>
    </div>
  </div>
</template>

<script setup>
import {
  PhPlus,
  PhHeart,
  PhChatCircleText,
  PhBookmarks,
  PhShareFat,
} from "@phosphor-icons/vue";
import { addFavorite, removeFavorite } from "@/api/operatieFavorite";
import { addLike, removeLike } from "@/api/operateLike";

// ---------- 四个操作的计数（父组件整体传入，接口返回后由父组件同步） ----------
const counters = defineModel("counters", {
  type: Object,
  default: () => ({
    likeCount: 0,
    commentCount: 0,
    favoriteCount: 0,
    shareCount: 0,
  }),
});

// ---------- 四个操作的状态（v-model 双向绑定，父组件持有最终数据） ----------
const liked = defineModel("liked", { type: Boolean, required: true });
const favorited = defineModel("favorited", { type: Boolean, required: true });
const commented = defineModel("commented", { type: Boolean, default: false });
const shared = defineModel("shared", { type: Boolean, default: false });

const props = defineProps({
  // 当前帖子的 id，调用点赞/收藏/评论/转发接口时拼 URL 用
  postId: {
    type: [String, Number],
    required: true,
  },
  ownerAvatar: {
    type: String,
    default: "",
  },
  ownerNickname: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["toggleComment"]);

// 独立切换方法，互不影响（后续可在方法里调用对应接口）
const toggleLike = async () => {
  //没有点赞的情况下,调用点赞接口
  if (!liked.value) {
    const data = await addLike(props.postId);
    liked.value = data.liked;
    counters.value = {
      ...counters.value,
      likeCount: data.likeCount,
    };

    //已经点赞的情况下,调用取消点赞接口
  } else {
    const data = await removeLike(props.postId);
    liked.value = data.liked;
    counters.value = {
      ...counters.value,
      likeCount: data.likeCount,
    };
  }
};

const toggleComment = () => {
  commented.value = !commented.value;
  // 通知父组件打开/关闭该帖子的评论面板
  emit("toggleComment", commented.value);
};

const toggleCollect = async () => {
  try {
    const data = favorited.value
      ? await removeFavorite(props.postId)
      : await addFavorite(props.postId);

    favorited.value = data.favorited;
    counters.value = {
      ...counters.value,
      favoriteCount: data.favoriteCount,
    };
  } catch (error) {
    console.error(error);
  }
};

const toggleShare = () => {
  shared.value = !shared.value;
};
</script>

<style scoped src="@/style/cardInfo.less"></style>
