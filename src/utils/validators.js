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
