/**
 * 分页工具
 * 统一 page/limit 解析与校验逻辑，替代各 route 中的重复实现。
 */

/**
 * 解析分页参数。
 * @param {object} query - req.query
 * @param {{ defaultPage?: number, defaultLimit?: number, maxLimit?: number }} [options]
 * @returns {{ page: number, limit: number } | null} 非法时返回 null
 */
function parsePagination(query, options = {}) {
  const { defaultPage = 1, defaultLimit = 20, maxLimit = 50 } = options;
  const page = Number(query.page ?? defaultPage);
  const limit = Number(query.limit ?? defaultLimit);
  if (!Number.isInteger(page) || page < 1 || !Number.isInteger(limit) || limit < 1) {
    return null;
  }
  return { page, limit: Math.min(limit, maxLimit) };
}

/**
 * 解析正整数 ID。
 * @param {*} value
 * @param {*} [fallback] - 解析失败时的返回值，默认 null
 * @returns {number | null}
 */
function parsePositiveInteger(value, fallback = null) {
  const parsed = Number(value ?? fallback);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback === null ? null : fallback;
}

module.exports = { parsePagination, parsePositiveInteger };
