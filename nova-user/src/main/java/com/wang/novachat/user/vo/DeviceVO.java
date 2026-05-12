package com.wang.novachat.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "在线设备信息")
@Data
public class DeviceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备类型: web/android/ios")
    private String deviceType;

    @Schema(description = "登录IP")
    private String ip;

    @Schema(description = "登录时间")
    private LocalDateTime loginAt;

    @Schema(description = "会话过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "是否为当前设备")
    private Boolean current;
}
