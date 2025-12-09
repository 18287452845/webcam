package webcam.service;

import java.nio.file.Path;

/**
 * 卡通图片生成服务接口
 * 使用阿里云人物动漫化API将用户照片转换为卡通风格
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface CartoonImageService {

    /**
     * 基于用户上传的照片生成卡通图片
     * 
     * @param userImagePath 用户上传的图片文件路径
     * @return 生成的卡通图片访问URL，如果生成失败返回null
     * @throws webcam.exception.BailianApiException 当API调用失败时
     */
    String generateCartoonImage(Path userImagePath);
    
    /**
     * 基于用户图片URL生成卡通图片
     * 
     * @param userImageUrl 用户图片的URL
     * @return 生成的卡通图片访问URL，如果生成失败返回null
     * @throws webcam.exception.BailianApiException 当API调用失败时
     */
    String generateCartoonImageFromUrl(String userImageUrl);
}

