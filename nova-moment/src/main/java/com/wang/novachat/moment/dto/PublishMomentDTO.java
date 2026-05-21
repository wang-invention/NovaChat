package com.wang.novachat.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "发布朋友圈动态请求")
@Data
public class PublishMomentDTO {

    @NotBlank(message = "内容不能为空")
    @Schema(description = "文字内容", example = "今天天气真好！")
    private String content;

    @Schema(description = "图片URL列表", example = "[\"https://example.com/img1.jpg\"]")
    private List<String> images;
}