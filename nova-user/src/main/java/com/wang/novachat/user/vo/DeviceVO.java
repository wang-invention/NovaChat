package com.wang.novachat.user.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线设备信息。
 */
@Data
public class DeviceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceId;

    private String deviceType;

    private String ip;

    private LocalDateTime loginAt;

    private LocalDateTime expireAt;

    /** 是否为当前请求所在设备 */
    private Boolean current;
}
