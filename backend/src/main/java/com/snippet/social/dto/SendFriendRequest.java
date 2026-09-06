package com.snippet.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 发起好友申请的参数。申请人身份从 JWT 获取，不由请求体传入。
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "发起好友申请参数")
public class SendFriendRequest {

    @NotNull(message = "目标用户 ID 不能为空")
    @Positive(message = "目标用户 ID 必须为正数")
    @Schema(description = "目标用户 ID", example = "43")
    private Long targetUserId;
}
