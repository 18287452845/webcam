package webcam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import webcam.config.R2Properties;
import webcam.exception.FileStorageException;
import webcam.service.impl.R2UploadServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * R2上传服务测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@DisplayName("R2UploadService Tests")
class R2UploadServiceTest {
    
    @Mock
    private S3Client s3Client;
    
    @Mock
    private R2Properties r2Properties;
    
    private R2UploadService r2UploadService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 配置R2属性
        when(r2Properties.getAccessKeyId()).thenReturn("test-key-id");
        when(r2Properties.getAccessKeySecret()).thenReturn("test-secret");
        when(r2Properties.getBucketName()).thenReturn("test-bucket");
        when(r2Properties.getEndpoint()).thenReturn("https://test.r2.cloudflarestorage.com");
        when(r2Properties.getPresignedUrlExpiration()).thenReturn(600L);
        
        r2UploadService = new R2UploadServiceImpl(r2Properties, s3Client);
    }
    
    @Test
    @DisplayName("Should throw exception when R2 configuration is missing access key")
    void testValidateR2ConfigurationMissingAccessKey() {
        // 模拟缺失accessKeyId
        when(r2Properties.getAccessKeyId()).thenReturn("");
        
        R2UploadService service = new R2UploadServiceImpl(r2Properties, s3Client);
        
        // 预期会抛出FileStorageException
        assertThrows(FileStorageException.class, () -> {
            service.uploadFromBase64("base64data", "test-key", "image/jpeg");
        });
    }
    
    @Test
    @DisplayName("Should throw exception when R2 configuration is missing bucket name")
    void testValidateR2ConfigurationMissingBucket() {
        // 模拟缺失bucketName
        when(r2Properties.getBucketName()).thenReturn("");
        
        R2UploadService service = new R2UploadServiceImpl(r2Properties, s3Client);
        
        // 预期会抛出FileStorageException
        assertThrows(FileStorageException.class, () -> {
            service.uploadFromBase64("base64data", "test-key", "image/jpeg");
        });
    }
}
