package webcam.service;

import java.nio.file.Path;

/**
 * 图像存储服务接口
 * 负责图像文件的存储和管理
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface ImageStorageService {

    /**
     * 保存Base64编码的图像数据到文件
     * 
     * @param base64Data Base64编码的图像数据（不包含data:image前缀）
     * @param fileName   文件名（包含扩展名）
     * @return 保存的文件路径
     * @throws webcam.exception.ImageProcessingException 当Base64解码失败时
     * @throws webcam.exception.FileStorageException     当文件保存失败时
     */
    Path saveBase64Image(String base64Data, String fileName);

    /**
     * 从图像数据中提取纯Base64字符串
     * 移除可能的"data:image/jpeg;base64,"前缀
     * 
     * @param imageData 原始图像数据字符串
     * @return 纯Base64字符串
     */
    String extractBase64Data(String imageData);

    /**
     * 生成图像的访问URL
     * 
     * @param fileName 文件名
     * @return 完整的访问URL
     */
    String getImageUrl(String fileName);

    /**
     * 验证图像数据是否有效
     * 
     * @param imageData 图像数据字符串
     * @throws IllegalArgumentException 当数据无效时
     */
    void validateImageData(String imageData);
}
