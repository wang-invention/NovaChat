package com.wang.novachat.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_call_record")
public class CallRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long callerId;

    private Long calleeId;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private String callType;

    @Version
    private Integer version;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}