package com.wang.novachat.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "文件上传结果")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "缩略图URL(图片)")
    private String thumbUrl;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件大小(字节)")
    private long size;

    @Schema(description = "文件MIME类型", example = "image/jpeg")
    private String contentType;
}
