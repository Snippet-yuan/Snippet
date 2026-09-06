package com.snippet.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 好友申请及其处理结果。
 */
@Getter
@AllArgsConstructor
@Schema(description = "好友申请响应")
public class FriendRequestResponse {

    @Schema(description = "申请记录 ID", example = "501")
    private Long requestId;

    @Schema(description = "申请状态", example = "PENDING")
    private String status;

    @Schema(description = "申请人摘要")
    private UserSummaryResponse requester;

    @Schema(description = "申请创建时间", example = "2026-09-06T15:30:00")
    private LocalDateTime createdAt;
}
