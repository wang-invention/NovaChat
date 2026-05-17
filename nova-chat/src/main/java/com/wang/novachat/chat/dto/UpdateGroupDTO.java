package com.wang.novachat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "更新群信息请求")
@Data
public class UpdateGroupDTO {

    @Schema(description = "群名称", example = "新群名称")
    @Size(max = 100, message = "群名称最多100个字符")
    private String name;

    @Schema(description = "群头像URL")
    private String avatar;

    @Schema(description = "群公告")
    @Size(max = 500, message = "群公告最多500个字符")
    private String announcement;

    @Schema(description = "最大成员数")
    private Integer maxMembers;
}