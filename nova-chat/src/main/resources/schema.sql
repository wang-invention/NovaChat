CREATE DATABASE IF NOT EXISTS nova_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nova_chat;

CREATE TABLE IF NOT EXISTS t_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_key VARCHAR(64) NOT NULL COMMENT '会话唯一标识: minId_maxId',
    user1_id BIGINT NOT NULL COMMENT '较小用户ID',
    user2_id BIGINT NOT NULL COMMENT '较大用户ID',
    last_message VARCHAR(500) DEFAULT '' COMMENT '最后一条消息预览',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    unread_count_user1 INT DEFAULT 0 COMMENT 'user1未读数',
    unread_count_user2 INT DEFAULT 0 COMMENT 'user2未读数',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conversation_key (conversation_key),
    KEY idx_user1 (user1_id),
    KEY idx_user2 (user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE IF NOT EXISTS t_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型: text/image/emoji/system',
    content TEXT COMMENT '消息内容',
    image_url VARCHAR(500) DEFAULT '' COMMENT '图片URL',
    quote_id BIGINT DEFAULT NULL COMMENT '引用消息ID',
    recalled TINYINT DEFAULT 0 COMMENT '是否已撤回: 0否1是',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_conversation_id (conversation_id),
    KEY idx_sender (sender_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE IF NOT EXISTS t_friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    remark VARCHAR(64) DEFAULT '' COMMENT '好友备注',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    KEY idx_user_id (user_id),
    KEY idx_friend_id (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

CREATE TABLE IF NOT EXISTS t_friend_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_user_id BIGINT NOT NULL COMMENT '申请人ID',
    to_user_id BIGINT NOT NULL COMMENT '被申请人ID',
    message VARCHAR(200) DEFAULT '' COMMENT '验证消息',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待处理 1已同意 2已拒绝',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_from (from_user_id),
    KEY idx_to (to_user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

CREATE TABLE IF NOT EXISTS t_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '群名称',
    avatar VARCHAR(500) DEFAULT '' COMMENT '群头像URL',
    owner_id BIGINT NOT NULL COMMENT '群主用户ID',
    announcement VARCHAR(500) DEFAULT '' COMMENT '群公告',
    max_members INT DEFAULT 200 COMMENT '最大成员数',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_owner (owner_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群聊表';

CREATE TABLE IF NOT EXISTS t_group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL COMMENT '群ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0成员 1管理员 2群主',
    nickname_in_group VARCHAR(64) DEFAULT '' COMMENT '群内昵称',
    muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_group_id (group_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';
