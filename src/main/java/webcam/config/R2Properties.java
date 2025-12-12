package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudflare R2配置属性
 * 用于配置R2对象存储的访问凭证和基本信息
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Component
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    
    private String accountId;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String region;
    private String endpoint;
    private long presignedUrlExpiration = 600; // 默认600秒
    
    public R2Properties() {
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public long getPresignedUrlExpiration() {
        return presignedUrlExpiration;
    }
    
    public void setPresignedUrlExpiration(long presignedUrlExpiration) {
        this.presignedUrlExpiration = presignedUrlExpiration;
    }
}
