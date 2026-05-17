package com.wang.novachat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "添加群成员请求")
@Data
public class AddGroupMemberDTO {

    @Schema(description = "要添加的用户ID列表", example = "[3,4,5]")
    @NotNull(message = "用户ID列表不能为空")
    @Size(min = 1, max = 100, message = "一次最多添加100个成员")
    private List<Long> userIds;
}