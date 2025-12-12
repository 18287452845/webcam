package webcam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import java.net.URI;

/**
 * Cloudflare R2 S3客户端配置
 * 使用AWS SDK for Java，因为Cloudflare R2与S3兼容
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Configuration
public class R2Config {
    
    @Autowired
    private R2Properties r2Properties;
    
    @Bean
    public S3Client s3Client() {
        // 创建AWS凭证
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Properties.getAccessKeyId(),
                r2Properties.getAccessKeySecret()
        );
        
        // 构建S3客户端
        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        
        // 设置自定义端点（R2特定的端点）
        if (r2Properties.getEndpoint() != null && !r2Properties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(r2Properties.getEndpoint()));
        }
        
        // 如果指定了区域，使用该区域
        if (r2Properties.getRegion() != null && !r2Properties.getRegion().isEmpty()) {
            builder.region(Region.of(r2Properties.getRegion()));
        } else {
            // 默认使用us-east-1
            builder.region(Region.US_EAST_1);
        }
        
        return builder.build();
    }
}
