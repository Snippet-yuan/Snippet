package com.snippet.post.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.snippet.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PostContentValidator {

    public static final int MAX_CONTENT_BYTES = 1024 * 1024;
    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final int MAX_DEPTH = 32;
    private static final int MAX_NODE_COUNT = 10_000;

    public void validate(JsonNode content, Integer schemaVersion) {
        if (schemaVersion == null || schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            throw badRequest("暂不支持该正文结构版本");
        }
        if (content == null || !content.isObject()) {
            throw badRequest("正文必须是 JSON 对象");
        }
        JsonNode type = content.get("type");
        if (type == null || !type.isTextual() || !"doc".equals(type.textValue())) {
            throw badRequest("正文根节点类型必须为 doc");
        }
        JsonNode children = content.get("children");
        if (children == null || !children.isArray()) {
            throw badRequest("正文必须包含 children 数组");
        }

        int contentBytes = content.toString().getBytes(StandardCharsets.UTF_8).length;
        if (contentBytes > MAX_CONTENT_BYTES) {
            throw badRequest("正文大小不能超过 1 MB");
        }

        validateNode(content, 0, new int[]{0});
    }

    private void validateNode(JsonNode node, int depth, int[] nodeCount) {
        if (depth > MAX_DEPTH) {
            throw badRequest("正文嵌套层级过深");
        }
        if (++nodeCount[0] > MAX_NODE_COUNT) {
            throw badRequest("正文节点数量超过限制");
        }
        if (node.isTextual() && containsUnsafeText(node.textValue())) {
            throw badRequest("正文不能包含可执行脚本或危险 HTML");
        }

        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                if (field.getKey().length() > 64) {
                    throw badRequest("正文字段名过长");
                }
                validateNode(field.getValue(), depth + 1, nodeCount);
            }
        } else if (node.isArray()) {
            var children = node.elements();
            while (children.hasNext()) {
                validateNode(children.next(), depth + 1, nodeCount);
            }
        }
    }

    private boolean containsUnsafeText(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("<script")
                || normalized.contains("</script")
                || normalized.contains("javascript:")
                || normalized.contains("vbscript:")
                || normalized.contains("data:text/html")
                || normalized.contains("<iframe")
                || normalized.contains("<object")
                || normalized.contains("<embed");
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }
}
