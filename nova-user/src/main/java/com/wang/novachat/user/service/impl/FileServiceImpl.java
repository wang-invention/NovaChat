package com.wang.novachat.user.service.impl;

import cn.hutool.core.date.DateUtil;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.user.config.MinioConfig;
import com.wang.novachat.user.dto.FileUploadResult;
import com.wang.novachat.user.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final int THUMB_WIDTH = 200;
    private static final int THUMB_HEIGHT = 200;
    private static final float THUMB_QUALITY = 0.7f;

    @Override
    public FileUploadResult uploadAvatar(Long userId, MultipartFile file) {
        validateImage(file, MAX_AVATAR_SIZE);
        String originName = generateObjectName(userId, file, "avatar");
        String thumbName = generateThumbObjectName(originName);

        try {
            uploadToMinio(originName, file);

            byte[] thumbBytes = generateThumb(file);
            ByteArrayInputStream thumbInputStream = new ByteArrayInputStream(thumbBytes);
            uploadToMinio(thumbName, thumbInputStream, thumbBytes.length, file.getContentType());

            log.info("[FileService] 头像上传成功: 原图={}, 缩略图={}", originName, thumbName);
            return new FileUploadResult(
                    getFileUrl(thumbName),
                    getFileUrl(thumbName),
                    thumbName,
                    thumbBytes.length,
                    file.getContentType()
            );
        } catch (Exception e) {
            log.error("[FileService] 头像上传失败: {}", originName, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "头像上传失败");
        }
    }

    @Override
    public FileUploadResult uploadImage(Long userId, MultipartFile file) {
        validateImage(file, MAX_IMAGE_SIZE);
        String originName = generateObjectName(userId, file, "image");
        String thumbName = generateThumbObjectName(originName);

        try {
            uploadToMinio(originName, file);

            byte[] thumbBytes = generateThumb(file);
            ByteArrayInputStream thumbInputStream = new ByteArrayInputStream(thumbBytes);
            uploadToMinio(thumbName, thumbInputStream, thumbBytes.length, file.getContentType());

            log.info("[FileService] 图片上传成功: 原图={}, 缩略图={}", originName, thumbName);
            return new FileUploadResult(
                    getFileUrl(originName),
                    getFileUrl(thumbName),
                    originName,
                    file.getSize(),
                    file.getContentType()
            );
        } catch (Exception e) {
            log.error("[FileService] 图片上传失败: {}", originName, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "图片上传失败");
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
        } catch (Exception e) {
            log.error("[FileService] 获取文件URL失败: {}", objectName, e);
            return null;
        }
    }

    private void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小超过限制");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能上传图片文件");
        }
    }

    private String generateObjectName(Long userId, MultipartFile file, String type) {
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String ext = getFileExtension(file.getOriginalFilename());
        return String.format("%s/%s/%d_%s.%s", type, date, userId, UUID.randomUUID().toString().substring(0, 8), ext);
    }

    private String generateThumbObjectName(String originName) {
        int lastDot = originName.lastIndexOf('.');
        if (lastDot > 0) {
            return originName.substring(0, lastDot) + "_thumb" + originName.substring(lastDot);
        }
        return originName + "_thumb";
    }

    private byte[] generateThumb(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Thumbnails.of(is)
                    .size(THUMB_WIDTH, THUMB_HEIGHT)
                    .outputQuality(THUMB_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(os);
            return os.toByteArray();
        }
    }

    private void uploadToMinio(String objectName, MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("[FileService] MinIO上传失败: {}", objectName, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    private void uploadToMinio(String objectName, InputStream is, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .stream(is, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("[FileService] MinIO上传失败: {}", objectName, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "jpg";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
