// utils/time.js
export function formatTime(isoString, format = "YYYY-MM-DD HH:mm:ss") {
  if (!isoString) return "";
  const date = new Date(isoString);

  const map = {
    YYYY: date.getFullYear(),
    MM: String(date.getMonth() + 1).padStart(2, "0"),
    DD: String(date.getDate()).padStart(2, "0"),
    HH: String(date.getHours()).padStart(2, "0"),
    mm: String(date.getMinutes()).padStart(2, "0"),
    ss: String(date.getSeconds()).padStart(2, "0"),
  };

  return format.replace(/YYYY|MM|DD|HH|mm|ss/g, (match) => map[match]);
}

// 使用
formatTime("2026-09-02T09:17:01.000Z", "YYYY-MM-DD HH"); // "2026-09-02 17:17:01"
formatTime("2026-09-02T09:17:01.000Z", "MM-DD"); // "09-02"
