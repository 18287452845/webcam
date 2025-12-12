package webcam.dto;

/**
 * 卡通图片生成结果数据类
 * 包含本地URL、R2 URL、预签名URL和二维码等信息
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public class CartoonImageResult {
    
    private String localUrl;           // 本地存储URL
    private String r2Url;              // R2对象存储URL（仅用于参考，不可直接访问需要认证）
    private String presignedUrl;       // R2预签名URL（可直接访问，600秒有效期）
    private String qrCodeBase64;       // 二维码Base64编码（data:image/png;base64,前缀）
    private String r2ObjectKey;        // R2对象key（UUID/时间戳格式）
    private long fileSize;             // 文件大小（字节）
    
    public CartoonImageResult() {
    }
    
    public CartoonImageResult(String localUrl, String r2Url, String presignedUrl, 
                             String qrCodeBase64, String r2ObjectKey, long fileSize) {
        this.localUrl = localUrl;
        this.r2Url = r2Url;
        this.presignedUrl = presignedUrl;
        this.qrCodeBase64 = qrCodeBase64;
        this.r2ObjectKey = r2ObjectKey;
        this.fileSize = fileSize;
    }
    
    public String getLocalUrl() {
        return localUrl;
    }
    
    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }
    
    public String getR2Url() {
        return r2Url;
    }
    
    public void setR2Url(String r2Url) {
        this.r2Url = r2Url;
    }
    
    public String getPresignedUrl() {
        return presignedUrl;
    }
    
    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
    
    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
    
    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
    
    public String getR2ObjectKey() {
        return r2ObjectKey;
    }
    
    public void setR2ObjectKey(String r2ObjectKey) {
        this.r2ObjectKey = r2ObjectKey;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
