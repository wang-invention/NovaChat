package com.wang.novachat.moment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "朋友圈动态")
@Data
public class MomentVO {

    @Schema(description = "动态ID")
    private Long id;

    @Schema(description = "发布者用户ID")
    private Long userId;

    @Schema(description = "发布者昵称")
    private String nickname;

    @Schema(description = "发布者用户名")
    private String username;

    @Schema(description = "发布者头像")
    private String avatar;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "点赞用户列表")
    private List<MomentLikeVO> likes;

    @Schema(description = "评论列表")
    private List<MomentCommentVO> comments;

    @Schema(description = "点赞数")
    private int likeCount;

    @Schema(description = "评论数")
    private int commentCount;

    @Schema(description = "当前用户是否已点赞")
    private boolean liked;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}