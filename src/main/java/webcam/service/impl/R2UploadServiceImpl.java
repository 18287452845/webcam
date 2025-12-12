package webcam.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import webcam.config.R2Properties;
import webcam.exception.FileStorageException;
import webcam.service.R2UploadService;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

/**
 * Cloudflare R2上传服务实现
 * 使用AWS SDK for Java与R2交互
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class R2UploadServiceImpl implements R2UploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(R2UploadServiceImpl.class);
    
    private final R2Properties r2Properties;
    private final S3Client s3Client;
    
    @Autowired
    public R2UploadServiceImpl(R2Properties r2Properties, S3Client s3Client) {
        this.r2Properties = r2Properties;
        this.s3Client = s3Client;
    }
    
    @Override
    public R2ObjectInfo uploadFile(Path filePath, String objectKey) {
        try {
            // 检查R2配置是否完整
            validateR2Configuration();
            
            if (!Files.exists(filePath)) {
                throw new FileStorageException("文件不存在: " + filePath);
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            logger.debug("Uploading file to R2: bucket={}, key={}, size={} bytes", 
                    r2Properties.getBucketName(), objectKey, fileContent.length);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Properties.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, 
                    RequestBody.fromBytes(fileContent));
            
            logger.info("File uploaded successfully to R2: key={}, ETag={}", 
                    objectKey, putObjectResponse.eTag());
            
            return new R2ObjectInfo(objectKey, r2Properties.getBucketName(), 
                    fileContent.length, contentType);
            
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error uploading file to R2: {}", objectKey, e);
            throw new FileStorageException("上传文件到R2失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public R2ObjectInfo uploadFromBase64(String base64Data, String objectKey, String contentType) {
        try {
            // 检查R2配置是否完整
            validateR2Configuration();
            
            // 移除可能的Data URI前缀
            String cleanBase64 = base64Data;
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            
            byte[] fileContent = Base64.getDecoder().decode(cleanBase64);
            
            logger.debug("Uploading base64 data to R2: bucket={}, key={}, size={} bytes", 
                    r2Properties.getBucketName(), objectKey, fileContent.length);
            
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Properties.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(fileContent));
            
            logger.info("Base64 data uploaded successfully to R2: key={}, ETag={}", 
                    objectKey, putObjectResponse.eTag());
            
            return new R2ObjectInfo(objectKey, r2Properties.getBucketName(), 
                    fileContent.length, contentType);
            
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error uploading base64 data to R2: {}", objectKey, e);
            throw new FileStorageException("上传Base64数据到R2失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getPresignedUrl(String objectKey, long expirationSeconds) {
        try {
            logger.debug("Generating presigned URL for R2 object: key={}, expiration={}s", 
                    objectKey, expirationSeconds);
            
            // 检查R2配置是否完整
            validateR2Configuration();
            
            // 创建S3 Presigner用于生成预签名URL
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    r2Properties.getAccessKeyId(),
                    r2Properties.getAccessKeySecret()
            );
            
            S3Presigner presigner = S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.US_EAST_1)
                    .endpointOverride(URI.create(r2Properties.getEndpoint()))
                    .build();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(r2Properties.getBucketName())
                    .key(objectKey)
                    .build();
            
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            logger.info("Presigned URL generated successfully: key={}, URL expires in {} seconds", 
                    objectKey, expirationSeconds);
            
            presigner.close();
            return presignedUrl;
            
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating presigned URL for R2 object: {}", objectKey, e);
            throw new FileStorageException("生成R2预签名URL失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证R2配置是否完整
     */
    private void validateR2Configuration() {
        if (r2Properties.getAccessKeyId() == null || r2Properties.getAccessKeyId().isEmpty()) {
            throw new FileStorageException("R2配置缺失: accessKeyId未设置");
        }
        if (r2Properties.getAccessKeySecret() == null || r2Properties.getAccessKeySecret().isEmpty()) {
            throw new FileStorageException("R2配置缺失: accessKeySecret未设置");
        }
        if (r2Properties.getBucketName() == null || r2Properties.getBucketName().isEmpty()) {
            throw new FileStorageException("R2配置缺失: bucketName未设置");
        }
        if (r2Properties.getEndpoint() == null || r2Properties.getEndpoint().isEmpty()) {
            throw new FileStorageException("R2配置缺失: endpoint未设置");
        }
    }
}
