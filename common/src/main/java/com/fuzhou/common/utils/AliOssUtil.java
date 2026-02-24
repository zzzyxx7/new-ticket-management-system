package com.fuzhou.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.fuzhou.common.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 阿里云 OSS 上传工具
 */
@Component
@Slf4j
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 上传文件到 OSS，并返回可访问的 URL
     *
     * @param bytes           文件字节数组
     * @param originalFilename 原始文件名，用于获取后缀
     * @return 文件在 OSS 上的完整访问 URL
     */
    public String upload(byte[] bytes, String originalFilename) {
        String endpoint = aliOssProperties.getEndpoint();
        String accessKeyId = aliOssProperties.getAccessKeyId();
        String accessKeySecret = aliOssProperties.getAccessKeySecret();
        String bucketName = aliOssProperties.getBucketName();

        // 1. 生成不重复的文件名：folder/yyyy/MM/dd/uuid.ext
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        LocalDate today = LocalDate.now();
        String folder = String.format("images/%d/%02d/%02d/", today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        String objectName = folder + UUID.randomUUID().toString().replace("-", "") + suffix;

        OSS ossClient = null;
        try {
            // 2. 创建 OSSClient 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 3. 上传
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));

            // 4. 拼接访问 URL，如：https://bucket.endpoint/objectName
            String url = String.format("https://%s.%s/%s", bucketName, endpoint, objectName);
            log.info("上传文件到 OSS 成功, url={}", url);
            return url;
        } catch (Exception e) {
            log.error("上传文件到 OSS 失败", e);
            throw new RuntimeException("上传文件失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}

