package webcam.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import webcam.service.FaceRecognitionService;
import webcam.service.ImageStorageService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Webcam控制器
 * 处理摄像头图像上传和阿里云百炼API调用
 * 采用分层架构，Controller只负责HTTP请求/响应，业务逻辑由Service层处理
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@RestController
@RequestMapping("/webcam")
public class WebcamController {

    private static final Logger logger = LoggerFactory.getLogger(WebcamController.class);

    private final ImageStorageService imageStorageService;
    private final FaceRecognitionService faceRecognitionService;

    /**
     * 构造函数，注入依赖的Service
     * 
     * @param imageStorageService    图像存储服务
     * @param faceRecognitionService 人脸识别服务
     */
    @Autowired
    public WebcamController(ImageStorageService imageStorageService,
            FaceRecognitionService faceRecognitionService) {
        this.imageStorageService = imageStorageService;
        this.faceRecognitionService = faceRecognitionService;
    }

    /**
     * 处理图像上传和阿里云百炼API调用
     *
     * @param imageData Base64编码的图像数据（可能包含data:image/png;base64,前缀）
     * @return 包含检测结果的JSON响应
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> processImage(
            @RequestParam("image") String imageData) {
        LocalDateTime startTime = LocalDateTime.now();
        String requestId = UUID.randomUUID().toString();
        logger.info("Received image processing request [RequestId: {}]", requestId);

        try {
            // 验证输入
            imageStorageService.validateImageData(imageData);

            // 提取Base64数据
            String base64Data = imageStorageService.extractBase64Data(imageData);

            // 生成文件名
            String fileName = UUID.randomUUID() + ".jpeg";

            // 保存图像文件（Service层会抛出异常，由GlobalExceptionHandler处理）
            Path filePath = imageStorageService.saveBase64Image(base64Data, fileName);
            logger.debug("Image saved: {} [RequestId: {}]", fileName, requestId);

            // 调用阿里云百炼API进行人脸检测和健康分析（Service层会抛出异常，由GlobalExceptionHandler处理）
            Map<String, Object> faceAttributes = faceRecognitionService.detectFaceAttributes(filePath);
            if (faceAttributes == null) {
                faceAttributes = new HashMap<>();
            } else {
                faceAttributes = new HashMap<>(faceAttributes);
            }

            // 构建图像URL
            String imageUrl = imageStorageService.getImageUrl(fileName);
            faceAttributes.put("img", imageUrl);

            // 百炼API已返回praise和healthAnalysis，无需额外处理

            // 构建成功响应
            ApiResponse<Map<String, Object>> response = ApiResponse.success(faceAttributes, startTime);
            response.setRequestId(requestId);

            // 如果没有检测到人脸，返回失败状态（但保持兼容性）
            if (faceAttributes.isEmpty()) {
                response.setResult("0");
                response.setMsg((Map<String, Object>) null);
                response.setErrorCode("NO_FACE_DETECTED");
                response.setErrorDetail("未检测到人脸，请确保照片中有人脸且清晰可见");
                logger.info("No face detected in image [RequestId: {}]", requestId);
            } else {
                logger.info("Successfully processed image: {} [RequestId: {}, ProcessingTime: {}ms]", 
                    fileName, requestId, response.getProcessingTime());
            }

            return ResponseEntity.ok()
                    .header("X-Request-Id", requestId)
                    .header("X-Processing-Time", String.valueOf(response.getProcessingTime()))
                    .header("Pragma", "No-cache")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Expires", "0")
                    .body(response);

        } catch (Exception e) {
            logger.error("Error processing image [RequestId: {}]", requestId, e);
            // 异常会被GlobalExceptionHandler处理
            throw e;
        }
    }
}
