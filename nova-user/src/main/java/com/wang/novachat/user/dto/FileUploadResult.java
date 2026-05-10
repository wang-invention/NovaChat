package com.wang.novachat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String url;
    private String thumbUrl;
    private String filename;
    private long size;
    private String contentType;
}
