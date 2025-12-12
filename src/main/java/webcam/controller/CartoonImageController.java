package webcam.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webcam.dto.CartoonImageResult;
import webcam.service.CartoonImageService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 卡通图片生成控制器
 * 提供卡通图片生成API，包含R2上传和预签名URL生成
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/cartoon")
public class CartoonImageController {
    
    private static final Logger logger = LoggerFactory.getLogger(CartoonImageController.class);
    
    private final CartoonImageService cartoonImageService;
    
    @Autowired
    public CartoonImageController(CartoonImageService cartoonImageService) {
        this.cartoonImageService = cartoonImageService;
    }
    
    /**
     * 从用户图片URL生成卡通图片
     * 返回卡通图片URL、R2预签名URL和二维码
     * 
     * @param imageUrl 用户图片的URL
     * @return 包含本地URL、R2预签名URL和二维码的响应
     */
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> generateCartoonImage(
            @RequestParam("imageUrl") String imageUrl) {
        
        Map<String, Object> response = new HashMap<>();
        String requestId = UUID.randomUUID().toString();
        
        logger.info("Received cartoon generation request [RequestId: {}, ImageUrl: {}]", requestId, imageUrl);
        
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                response.put("success", false);
                response.put("error", "imageUrl参数不能为空");
                response.put("requestId", requestId);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 生成卡通图片
            CartoonImageResult result = cartoonImageService.generateCartoonImageFromUrl(imageUrl);
            
            if (result == null) {
                response.put("success", false);
                response.put("error", "生成卡通图片失败");
                response.put("requestId", requestId);
                logger.warn("Cartoon generation failed: result is null [RequestId: {}]", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            // 构建成功响应
            response.put("success", true);
            response.put("data", new HashMap<String, Object>() {{
                put("localUrl", result.getLocalUrl());
                put("r2ObjectKey", result.getR2ObjectKey());
                put("presignedUrl", result.getPresignedUrl());
                put("qrCodeBase64", result.getQrCodeBase64());
                put("fileSize", result.getFileSize());
            }});
            response.put("requestId", requestId);
            
            logger.info("Cartoon image generated successfully [RequestId: {}, FileSize: {} bytes]", 
                    requestId, result.getFileSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating cartoon image [RequestId: {}]", requestId, e);
            response.put("success", false);
            response.put("error", "生成卡通图片时发生错误: " + e.getMessage());
            response.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 从本地文件路径生成卡通图片
     * （仅用于开发和内部测试）
     * 
     * @param filePath 本地文件路径
     * @return 包含本地URL、R2预签名URL和二维码的响应
     */
    @PostMapping(value = "/generate-from-file", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> generateCartoonImageFromFile(
            @RequestParam("filePath") String filePath) {
        
        Map<String, Object> response = new HashMap<>();
        String requestId = UUID.randomUUID().toString();
        
        logger.info("Received cartoon generation request from file [RequestId: {}, FilePath: {}]", 
                requestId, filePath);
        
        try {
            if (filePath == null || filePath.isEmpty()) {
                response.put("success", false);
                response.put("error", "filePath参数不能为空");
                response.put("requestId", requestId);
                return ResponseEntity.badRequest().body(response);
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                response.put("success", false);
                response.put("error", "指定的文件不存在: " + filePath);
                response.put("requestId", requestId);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 生成卡通图片
            CartoonImageResult result = cartoonImageService.generateCartoonImage(path);
            
            if (result == null) {
                response.put("success", false);
                response.put("error", "生成卡通图片失败");
                response.put("requestId", requestId);
                logger.warn("Cartoon generation failed: result is null [RequestId: {}]", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            // 构建成功响应
            response.put("success", true);
            response.put("data", new HashMap<String, Object>() {{
                put("localUrl", result.getLocalUrl());
                put("r2ObjectKey", result.getR2ObjectKey());
                put("presignedUrl", result.getPresignedUrl());
                put("qrCodeBase64", result.getQrCodeBase64());
                put("fileSize", result.getFileSize());
            }});
            response.put("requestId", requestId);
            
            logger.info("Cartoon image generated successfully [RequestId: {}, FileSize: {} bytes]", 
                    requestId, result.getFileSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating cartoon image [RequestId: {}]", requestId, e);
            response.put("success", false);
            response.put("error", "生成卡通图片时发生错误: " + e.getMessage());
            response.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
