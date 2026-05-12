package com.wang.novachat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "添加好友请求")
@Data
public class AddFriendDTO {

    @Schema(description = "目标用户ID", example = "2")
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;

    @Schema(description = "申请留言", example = "你好,我是张三")
    private String message;
}
