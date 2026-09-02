/**
 * Snippet 数据模型定义
 *
 * 统一描述所有数据的形状。store 和页面都从这里取类型/初始值，
 * 保证前后端字段一致。
 */

// ============================================================
// 身份数据（用户）
// ============================================================

/**
 * 当前登录用户（身份数据，放全局 user store）
 * @typedef {Object} UserProfile
 * @property {string} id             用户 ID
 * @property {string} email          邮箱账号（登录账号）
 * @property {string} nickname       昵称
 * @property {string} avatar         头像图片 URL
 * @property {string} background     主页背景图片 URL
 * @property {string} bio            个性签名
 * @property {string} createdAt      注册时间（ISO 字符串）
 * @property {number} followersCount 粉丝数
 * @property {number} followingCount 关注数
 */
export const emptyUserProfile = () => ({
  id: "",
  email: "",
  nickname: "",
  avatar: "",
  background: "",
  bio: "",
  createdAt: "",
  followersCount: 0,
  followingCount: 0,
});

// ============================================================
// 业务数据：好友
// ============================================================

/**
 * 好友列表项（业务数据，放 friends store）
 * @typedef {Object} Friend
 * @property {string} id          好友记录 ID
 * @property {string} userId      好友的用户 ID
 * @property {string} nickname    好友昵称
 * @property {string} avatar      好友头像 URL
 * @property {"ONLINE"|"OFFLINE"} onlineStatus 在线状态
 * @property {string} [lastMessage] 最后一条消息预览
 */
export const emptyFriend = () => ({
  id: "",
  userId: "",
  nickname: "",
  avatar: "",
  onlineStatus: "OFFLINE",
  lastMessage: "",
});

// ============================================================
// 业务数据：聊天消息
// ============================================================

/**
 * 一条聊天消息（业务数据，放 messages store）
 * @typedef {Object} Message
 * @property {string} id         消息 ID
 * @property {string} conversationId 会话 ID（同与同一好友的聊天）
 * @property {string} senderId   发送者用户 ID
 * @property {string} receiverId 接收者用户 ID
 * @property {string} content    消息内容（文本）
 * @property {string} sentAt     发送时间（ISO 字符串）
 */
export const emptyMessage = () => ({
  id: "",
  conversationId: "",
  senderId: "",
  receiverId: "",
  content: "",
  sentAt: "",
});

// ============================================================
// 业务数据：帖子
// ============================================================

/**
 * 用户发布的帖子（业务数据，放 posts store）
 * 帖子由 图片 + 标题 + 描述 组成
 * @typedef {Object} Post
 * @property {string} id            帖子 ID
 * @property {string} ownerId       作者用户 ID
 * @property {string} ownerNickname 作者昵称
 * @property {string} ownerAvatar   作者头像
 * @property {string} title         标题
 * @property {string} description   描述内容
 * @property {string[]} images      图片 URL 列表
 * @property {PostCounters} counters 点赞/收藏/评论/转发数
 * @property {boolean} liked        当前用户是否已点赞
 * @property {boolean} favorited    当前用户是否已收藏
 * @property {string} createdAt     发布时间
 */
export const emptyPost = () => ({
  id: "",
  ownerId: "",
  ownerNickname: "",
  ownerAvatar: "",
  title: "",
  description: "",
  images: [],
  counters: emptyPostCounters(),
  liked: false,
  favorited: false,
  createdAt: "",
});

/**
 * 帖子计数（点赞/收藏/评论/转发）
 * @typedef {Object} PostCounters
 * @property {number} likeCount    点赞数
 * @property {number} favoriteCount 收藏数
 * @property {number} commentCount 评论数
 * @property {number} shareCount   转发数
 */
export const emptyPostCounters = () => ({
  likeCount: 0,
  favoriteCount: 0,
  commentCount: 0,
  shareCount: 0,
});

// ============================================================
// 业务数据：评价 / 评论
// ============================================================

/**
 * 用户对别人帖子的评价（业务数据，放 comments store）
 * @typedef {Object} Comment
 * @property {string} id            评价 ID
 * @property {string} postId        被评价的帖子 ID
 * @property {string} authorId      评价者用户 ID
 * @property {string} authorNickname 评价者昵称
 * @property {string} authorAvatar  评价者头像
 * @property {string} content       评价内容
 * @property {string} createdAt     评价时间
 */
export const emptyComment = () => ({
  id: "",
  postId: "",
  authorId: "",
  authorNickname: "",
  authorAvatar: "",
  content: "",
  createdAt: "",
});
