package webcam.service;

import java.nio.file.Path;

/**
 * Cloudflare R2上传服务接口
 * 用于上传文件到R2对象存储并生成预签名URL
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface R2UploadService {
    
    /**
     * 上传文件到R2
     * 
     * @param filePath 本地文件路径
     * @param objectKey R2中的对象key（推荐使用UUID或时间戳避免可猜测）
     * @return 上传成功后的对象信息
     * @throws webcam.exception.FileStorageException 当上传失败时
     */
    R2ObjectInfo uploadFile(Path filePath, String objectKey);
    
    /**
     * 获取R2对象的预签名URL
     * 
     * @param objectKey R2中的对象key
     * @param expirationSeconds URL有效期（秒）
     * @return 预签名URL
     * @throws webcam.exception.FileStorageException 当生成URL失败时
     */
    String getPresignedUrl(String objectKey, long expirationSeconds);
    
    /**
     * 从Base64数据上传文件到R2
     * 
     * @param base64Data Base64编码的文件数据
     * @param objectKey R2中的对象key
     * @param contentType 文件内容类型（如 application/json, image/jpeg等）
     * @return 上传成功后的对象信息
     * @throws webcam.exception.FileStorageException 当上传失败时
     */
    R2ObjectInfo uploadFromBase64(String base64Data, String objectKey, String contentType);
    
    /**
     * R2对象信息数据类
     */
    class R2ObjectInfo {
        private String objectKey;
        private String bucket;
        private long size;
        private String contentType;
        private String presignedUrl;
        
        public R2ObjectInfo(String objectKey, String bucket, long size, String contentType) {
            this.objectKey = objectKey;
            this.bucket = bucket;
            this.size = size;
            this.contentType = contentType;
        }
        
        public String getObjectKey() {
            return objectKey;
        }
        
        public String getBucket() {
            return bucket;
        }
        
        public long getSize() {
            return size;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public String getPresignedUrl() {
            return presignedUrl;
        }
        
        public void setPresignedUrl(String presignedUrl) {
            this.presignedUrl = presignedUrl;
        }
    }
}
