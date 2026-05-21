package com.wang.novachat.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "朋友圈评论请求")
@Data
public class MomentCommentDTO {

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", example = "真不错！")
    private String content;

    @Schema(description = "回复目标用户ID（回复某人的评论时传入）")
    private Long replyToUserId;
}