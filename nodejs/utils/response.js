function success(data = null, message = "ok") {
  return { code: 0, message, data };
}

function fail(message = "error", code = 1, data = null) {
  return { code, message, data };
}

module.exports = { success, fail };
