package com.wang.novachat.user.service;

import com.wang.novachat.user.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResult uploadAvatar(Long userId, MultipartFile file);

    FileUploadResult uploadImage(Long userId, MultipartFile file);

    String getFileUrl(String objectName);
}
