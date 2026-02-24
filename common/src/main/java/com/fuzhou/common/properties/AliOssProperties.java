package com.fuzhou.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 */
@Component
@ConfigurationProperties(prefix = "fuzhou.alioss")
@Data
public class AliOssProperties {

    /**
     * OSS 服务访问域名，例如 oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;
}

