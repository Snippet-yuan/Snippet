// 引入 validate.js：一个通用表单校验库
// 用法：validate(要校验的数据, 校验规则) -> 通过返回 undefined，不通过返回错误对象
import validate from "validate.js";

// 登录表单的校验规则配置
// 结构：{ 字段名: { 校验器: { 配置项 } } }
const loginConstraints = {
  // 邮箱字段的校验规则
  email: {
    // presence：必填校验。allowEmpty: false 表示不允许为空
    // message：不满足时返回的错误文案
    presence: { allowEmpty: false, message: "请输入邮箱" },
    // email：格式校验，validate.js 内置的邮箱正则，不需要自己写正则
    email: { message: "邮箱格式不正确" },
  },
  // 密码字段的校验规则
  password: {
    // presence：必填，不允许为空
    presence: { allowEmpty: false, message: "请输入密码" },
    // length：长度校验。minimum: 6 表示最少 6 位
    // validate.js 会把传入的值转换成字符串再数长度
    length: { minimum: 6, message: "密码至少 6 位" },
  },
};

/**
 * 校验登录表单
 * @param {{ email: string, password: string }} 要校验的表单数据
 * @returns {undefined | { email?: string[], password?: string[] }}
 *   - undefined：校验通过
 *   - 对象：校验失败，形如 { email: ["请输入邮箱"], password: ["密码至少 6 位"] }
 *     每个字段的值是数组，数组里是命中的所有错误文案
 */
export function validateLoginForm({ email, password }) {
  // validate(数据, 规则)：用 loginConstraints 逐条检查 email 和 password
  // 全部通过 -> undefined；有错误 -> { 字段: [错误文案, ...] }
  return validate({ email, password }, loginConstraints);
}

// 注册表单的校验规则配置
const registerConstraints = {
  // 昵称：必填 + 长度限制
  nickname: {
    presence: { allowEmpty: false, message: "请输入昵称" },
    length: {
      minimum: 2,
      maximum: 20,
      message: "昵称需为 2-20 个字符",
    },
  },
  // 邮箱字段的校验规则
  email: {
    presence: { allowEmpty: false, message: "请输入邮箱" },
    email: { message: "邮箱格式不正确" },
  },
  // 密码字段的校验规则
  password: {
    presence: { allowEmpty: false, message: "请输入密码" },
    length: { minimum: 6, message: "密码至少 6 位" },
  },
  // 确认密码：与 password 字段必须一致
  confirmPassword: {
    presence: { allowEmpty: false, message: "请再次输入密码" },
    equality: {
      attribute: "password",
      message: "两次输入的密码不一致",
    },
  },
};

/**
 * 校验注册表单
 * @param {{ nickname: string, email: string, password: string, confirmPassword: string }} 要校验的表单数据
 * @returns {undefined | { nickname?: string[], email?: string[], password?: string[], confirmPassword?: string[] }}
 *   - undefined：校验通过
 *   - 对象：校验失败，形如 { confirmPassword: ["两次输入的密码不一致"] }
 */
export function validateRegisterForm({
  nickname,
  email,
  password,
  confirmPassword,
}) {
  return validate(
    { nickname, email, password, confirmPassword },
    registerConstraints,
  );
}

// 创建帖子表单的校验规则
const createPostConstraints = {
  title: {
    presence: { allowEmpty: false, message: "请输入标题" },
    length: { maximum: 100, message: "标题最多 100 个字符" },
  },
  description: {
    length: { maximum: 2000, message: "描述最多 2000 个字符" },
  },
};

/**
 * 校验创建帖子表单
 * @param {{ title: string, description?: string }} 要校验的表单数据
 * @returns {undefined | { title?: string[], description?: string[] }}
 */
export function validateCreatePostForm({ title, description }) {
  return validate({ title, description }, createPostConstraints);
}
