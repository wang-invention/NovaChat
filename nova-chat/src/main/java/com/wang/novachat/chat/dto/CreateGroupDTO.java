package com.wang.novachat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "创建群聊请求")
@Data
public class CreateGroupDTO {

    @Schema(description = "群名称", example = "技术交流群")
    @NotBlank(message = "群名称不能为空")
    @Size(max = 100, message = "群名称最多100个字符")
    private String name;

    @Schema(description = "群头像URL", example = "")
    private String avatar;

    @Schema(description = "初始成员用户ID列表(不含群主)", example = "[2,3,4]")
    @NotNull(message = "成员列表不能为空")
    @Size(min = 1, max = 199, message = "初始成员数量1-199")
    private List<Long> memberIds;

    @Schema(description = "群最大成员数", example = "200")
    @NotNull(message = "群最大成员数不能为空")
    @Size(min = 1, max = 200, message = "群最大成员数1-200")
    private Integer MaxMembers;
}