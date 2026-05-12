package com.wang.novachat.chat.dto;

import lombok.Data;

@Data
public class CallSignalDTO {

    private String type;

    private Long to;

    private Long callId;

    private String sdp;

    private String candidate;

    private String sdpMid;

    private Integer sdpMLineIndex;
}