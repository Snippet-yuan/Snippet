package com.snippet.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应外壳。具体业务 DTO 由各业务模块自行定义。
 */
@Getter
@AllArgsConstructor
@Schema(description = "统一接口响应")
public class CommonResult<T> {

    @Schema(description = "业务状态码，0 表示成功", example = "0")
    private final int code;

    @Schema(description = "响应说明", example = "操作成功")
    private final String message;

    @Schema(description = "业务数据，具体结构由接口定义")
    private final T data;

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(0, "操作成功", data);
    }

    public static CommonResult<Void> success() {
        return new CommonResult<>(0, "操作成功", null);
    }

    public static <T> CommonResult<T> failed(int code, String message) {
        return new CommonResult<>(code, message, null);
    }
}
