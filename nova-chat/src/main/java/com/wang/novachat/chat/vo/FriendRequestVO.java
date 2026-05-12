package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "好友申请信息")
@Data
public class FriendRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "申请人用户ID")
    private Long fromUserId;

    @Schema(description = "申请人昵称")
    private String fromNickname;

    @Schema(description = "申请人头像URL")
    private String fromAvatar;

    @Schema(description = "申请人用户名")
    private String fromUsername;

    @Schema(description = "接收人用户ID")
    private Long toUserId;

    @Schema(description = "申请留言")
    private String message;

    @Schema(description = "状态: 0待处理 1已接受 2已拒绝")
    private Integer status;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;
}
