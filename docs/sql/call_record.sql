-- 通话记录表
CREATE TABLE IF NOT EXISTS `t_call_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `caller_id`     BIGINT       NOT NULL                COMMENT '发起方用户ID',
    `callee_id`     BIGINT       NOT NULL                COMMENT '接收方用户ID',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'missed' COMMENT '通话状态: calling/ongoing/ended/missed/rejected/busy',
    `start_time`    DATETIME     DEFAULT NULL            COMMENT '通话开始时间（接听时间）',
    `end_time`      DATETIME     DEFAULT NULL            COMMENT '通话结束时间',
    `duration`      INT          DEFAULT 0               COMMENT '通话时长（秒）',
    `call_type`     VARCHAR(10)  NOT NULL DEFAULT 'audio' COMMENT '通话类型: audio/video',
    `version`       INT          NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_caller_id` (`caller_id`),
    INDEX `idx_callee_id` (`callee_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通话记录表';

-- 如果表已存在，添加 version 列（修复 MyBatis-Plus 乐观锁报错）
ALTER TABLE `t_call_record` ADD COLUMN IF NOT EXISTS `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `call_type`;