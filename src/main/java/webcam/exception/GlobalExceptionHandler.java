package webcam.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import webcam.controller.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器
 * 使用@ControllerAdvice统一处理应用程序中的异常
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理图像处理异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleImageProcessingException(
            ImageProcessingException ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.error("Image processing error [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "IMAGE_PROCESSING_ERROR",
            "图像处理失败: " + ex.getMessage()
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", response.getErrorDetail());
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 处理Face++ API异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(FacePlusPlusApiException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleFacePlusPlusApiException(
            FacePlusPlusApiException ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.error("Face++ API error [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "FACE_API_ERROR",
            "人脸识别服务调用失败: " + ex.getMessage()
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", response.getErrorDetail());
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 处理文件存储异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleFileStorageException(
            FileStorageException ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.error("File storage error [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "FILE_STORAGE_ERROR",
            "文件保存失败: " + ex.getMessage()
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", response.getErrorDetail());
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 处理文件大小超限异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.warn("Upload file size exceeded [RequestId: {}]: {}", requestId, ex.getMessage());

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "FILE_SIZE_EXCEEDED",
            "上传文件大小超过限制，请选择较小的图片（建议小于10MB）"
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", response.getErrorDetail());
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 处理参数异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.warn("Illegal argument [RequestId: {}]: {}", requestId, ex.getMessage());

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "INVALID_PARAMETER",
            "参数错误: " + ex.getMessage()
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", response.getErrorDetail());
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 处理所有未捕获的异常
     * 
     * @param ex      异常对象
     * @param request Web请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleGlobalException(
            Exception ex, WebRequest request) {
        String requestId = generateRequestId();
        logger.error("Unexpected error occurred [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            "INTERNAL_SERVER_ERROR",
            "系统错误，请稍后重试。如问题持续存在，请联系技术支持并提供RequestId: " + requestId
        );
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());

        // 兼容旧格式：将错误信息放入msg字段
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("error", "系统错误，请稍后重试");
        response.setMsg(errorMsg);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Request-Id", requestId)
                .header("X-Error-Code", response.getErrorCode())
                .body(response);
    }

    /**
     * 生成请求ID
     * 
     * @return 请求ID字符串
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
