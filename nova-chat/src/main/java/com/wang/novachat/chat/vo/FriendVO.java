package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "好友信息")
@Data
public class FriendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "好友用户ID")
    private Long friendId;

    @Schema(description = "好友昵称")
    private String nickname;

    @Schema(description = "好友用户名")
    private String username;

    @Schema(description = "好友头像URL")
    private String avatar;

    @Schema(description = "好友备注")
    private String remark;

    @Schema(description = "添加时间")
    private LocalDateTime addTime;

    @Schema(description = "首字母(用于通讯录分组)")
    private String initial;
}
