/**
 * 统一响应格式
 * 成功：{ code: 0, message: "ok", data }
 * 失败：{ code: 非 0, message, data }
 */

/**
 * 成功响应体。
 * @param {*} [data=null]
 * @param {string} [message="ok"]
 */
function success(data = null, message = "ok") {
  return { code: 0, message, data };
}

/**
 * 失败响应体。
 * @param {string} [message="error"]
 * @param {number} [code=1]
 * @param {*} [data=null]
 */
function fail(message = "error", code = 1, data = null) {
  return { code, message, data };
}

module.exports = { success, fail };
