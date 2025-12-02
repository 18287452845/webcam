package webcam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webcam.config.UploadProperties;
import webcam.exception.FileStorageException;
import webcam.exception.ImageProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 图像存储服务实现
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageStorageServiceImpl.class);

    private final UploadProperties uploadProperties;

    @Autowired
    public ImageStorageServiceImpl(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public Path saveBase64Image(String base64Data, String fileName) {
        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadProperties.getPath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // 解码Base64数据
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                logger.error("Failed to decode Base64 data", e);
                throw new ImageProcessingException("Base64解码失败，图像数据格式错误", e);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, imageBytes);
            logger.debug("Image saved to: {}", filePath);

            return filePath;

        } catch (IOException e) {
            logger.error("Failed to save image file: {}", fileName, e);
            throw new FileStorageException("文件保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractBase64Data(String imageData) {
        if (imageData == null) {
            return null;
        }

        // 移除可能的data:image前缀
        if (imageData.contains(",")) {
            return imageData.substring(imageData.indexOf(",") + 1);
        }

        return imageData;
    }

    @Override
    public String getImageUrl(String fileName) {
        return uploadProperties.getBaseUrl() + fileName;
    }

    @Override
    public void validateImageData(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            throw new IllegalArgumentException("图像数据不能为空");
        }

        // 提取Base64数据
        String base64Data = extractBase64Data(imageData);

        // 验证Base64格式
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的图像数据格式");
        }

        // 可以添加更多验证，如检查Base64字符串长度等
        if (base64Data.length() < 100) {
            throw new IllegalArgumentException("图像数据过小，可能不是有效的图像");
        }
    }
}
