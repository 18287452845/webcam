package webcam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import webcam.config.CartoonApiProperties;
import webcam.config.R2Properties;
import webcam.config.UploadProperties;
import webcam.dto.CartoonImageResult;
import webcam.exception.BailianApiException;
import webcam.exception.FileStorageException;
import webcam.service.CartoonImageService;
import webcam.service.ImageStorageService;
import webcam.service.QrCodeService;
import webcam.service.R2UploadService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 卡通图片生成服务实现
 * 使用阿里云人物动漫化API将用户照片转换为卡通风格
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class CartoonImageServiceImpl implements CartoonImageService {

    private static final Logger logger = LoggerFactory.getLogger(CartoonImageServiceImpl.class);

    private final CartoonApiProperties cartoonApiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ImageStorageService imageStorageService;
    private final UploadProperties uploadProperties;
    private final R2UploadService r2UploadService;
    private final R2Properties r2Properties;
    private final QrCodeService qrCodeService;

    @Autowired
    public CartoonImageServiceImpl(
            CartoonApiProperties cartoonApiProperties,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            ImageStorageService imageStorageService,
            UploadProperties uploadProperties,
            R2UploadService r2UploadService,
            R2Properties r2Properties,
            QrCodeService qrCodeService) {
        this.cartoonApiProperties = cartoonApiProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.imageStorageService = imageStorageService;
        this.uploadProperties = uploadProperties;
        this.r2UploadService = r2UploadService;
        this.r2Properties = r2Properties;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public CartoonImageResult generateCartoonImage(Path userImagePath) {
        try {
            logger.info("Generating cartoon image from: {}", userImagePath);
            
            // 读取图片并转换为Base64
            byte[] imageBytes = Files.readAllBytes(userImagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 调用阿里云人物动漫化API（使用base64）
            String cartoonImageUrl = callCartoonApiWithBase64(base64Image);
            
            if (cartoonImageUrl == null || cartoonImageUrl.isEmpty()) {
                logger.warn("Cartoon API returned empty URL");
                return null;
            }
            
            // 下载生成的卡通图片并保存到本地
            CartoonImageResult result = downloadAndSaveCartoonImage(cartoonImageUrl);
            
            logger.info("Cartoon image generated successfully: local={}, r2={}", 
                    result.getLocalUrl(), result.getR2ObjectKey());
            return result;
            
        } catch (BailianApiException e) {
            logger.error("Error calling cartoon API", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error generating cartoon image from path: {}", userImagePath, e);
            throw new BailianApiException("生成卡通图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public CartoonImageResult generateCartoonImageFromUrl(String userImageUrl) {
        try {
            logger.info("Generating cartoon image from URL: {}", userImageUrl);
            
            // 检查是否是本地URL，如果是则使用base64
            if (isLocalUrl(userImageUrl)) {
                // 从URL提取本地路径
                Path localImagePath = extractLocalPathFromUrl(userImageUrl);
                if (localImagePath != null && Files.exists(localImagePath)) {
                    return generateCartoonImage(localImagePath);
                }
            }
            
            // 对于公网URL，直接使用URL调用API
            String cartoonImageUrl = callCartoonApi(userImageUrl);
            
            if (cartoonImageUrl == null || cartoonImageUrl.isEmpty()) {
                logger.warn("Cartoon API returned empty URL");
                return null;
            }
            
            // 下载生成的卡通图片并保存到本地
            CartoonImageResult result = downloadAndSaveCartoonImage(cartoonImageUrl);
            
            logger.info("Cartoon image generated successfully: local={}, r2={}", 
                    result.getLocalUrl(), result.getR2ObjectKey());
            return result;
            
        } catch (Exception e) {
            logger.error("Error generating cartoon image from URL: {}", userImageUrl, e);
            throw new BailianApiException("从URL生成卡通图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查URL是否是本地URL（localhost或127.0.0.1）
     */
    private boolean isLocalUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.contains("localhost") || url.contains("127.0.0.1") || url.startsWith("/");
    }

    /**
     * 调用阿里云人物动漫化API
     * 
     * @param imageUrl 用户图片的URL
     * @return 生成的卡通图片URL
     */
    private String callCartoonApi(String imageUrl) {
        try {
            // 构建请求体（根据阿里云百炼图片生成API格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", cartoonApiProperties.getModel());
            
            // 构建输入参数（按照API文档格式）
            Map<String, Object> input = new HashMap<>();
            
            // 添加图片URL
            input.put("image_url", imageUrl);
            
            // 添加风格索引
            if (cartoonApiProperties.getStyleIndex() != null) {
                input.put("style_index", cartoonApiProperties.getStyleIndex());
            }
            
            requestBody.put("input", input);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cartoonApiProperties.getApiKey());
            headers.set("X-DashScope-Async", "enable"); // 启用异步模式
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.debug("Calling cartoon API: {} with image URL: {}", cartoonApiProperties.getEndpoint(), imageUrl);
            logger.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<String> response = restTemplate.exchange(
                    cartoonApiProperties.getEndpoint(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            
            logger.debug("API response status: {}, body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = response.getBody() != null ? response.getBody() : "Unknown error";
                throw new BailianApiException("卡通图片API调用失败，状态码: " + response.getStatusCode() + ", 响应: " + errorMsg);
            }
            
            // 解析响应
            return parseApiResponse(response.getBody());
            
        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error calling cartoon API", e);
            throw new BailianApiException("调用卡通图片API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用阿里云人物动漫化API（使用base64编码的图片）
     * 
     * @param base64Image Base64编码的图片数据
     * @return 生成的卡通图片URL
     */
    private String callCartoonApiWithBase64(String base64Image) {
        try {
            // 构建请求体（按照API文档格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", cartoonApiProperties.getModel());
            
            // 构建输入参数
            Map<String, Object> input = new HashMap<>();
            
            // 使用base64格式的图片（data URI格式）
            input.put("image_url", "data:image/jpeg;base64," + base64Image);
            
            // 添加风格索引
            if (cartoonApiProperties.getStyleIndex() != null) {
                input.put("style_index", cartoonApiProperties.getStyleIndex());
            }
            
            requestBody.put("input", input);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cartoonApiProperties.getApiKey());
            headers.set("X-DashScope-Async", "enable"); // 启用异步模式
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.debug("Calling cartoon API with base64 image");
            logger.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<String> response = restTemplate.exchange(
                    cartoonApiProperties.getEndpoint(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            
            logger.debug("API response status: {}, body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = response.getBody() != null ? response.getBody() : "Unknown error";
                throw new BailianApiException("卡通图片API调用失败，状态码: " + response.getStatusCode() + ", 响应: " + errorMsg);
            }
            
            // 解析响应
            return parseApiResponse(response.getBody());
            
        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error calling cartoon API with base64", e);
            throw new BailianApiException("调用卡通图片API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析API响应，提取生成的图片URL或task_id
     * 异步API可能返回task_id，需要轮询获取结果
     */
    private String parseApiResponse(String apiResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(apiResponse);
            
            // 检查是否有错误
            if (jsonNode.has("code") && !jsonNode.get("code").asText().equals("Success")) {
                String errorMsg = jsonNode.has("message") 
                    ? jsonNode.get("message").asText() 
                    : "API调用失败";
                logger.error("Cartoon API returned error: {}", errorMsg);
                throw new BailianApiException("卡通图片API返回错误: " + errorMsg);
            }
            
            // 异步API返回task_id的情况
            if (jsonNode.has("output") && jsonNode.get("output").has("task_id")) {
                String taskId = jsonNode.get("output").get("task_id").asText();
                logger.info("Received task_id from async API: {}", taskId);
                // 轮询获取结果
                return pollTaskResult(taskId);
            }
            
            // 同步返回结果的情况
            if (jsonNode.has("output")) {
                JsonNode output = jsonNode.get("output");
                if (output.has("results") && output.get("results").isArray() && output.get("results").size() > 0) {
                    JsonNode firstResult = output.get("results").get(0);
                    if (firstResult.has("url")) {
                        return firstResult.get("url").asText();
                    }
                }
            }
            
            // 兼容其他可能的响应格式
            if (jsonNode.has("data")) {
                JsonNode data = jsonNode.get("data");
                if (data.has("image_url")) {
                    return data.get("image_url").asText();
                }
                if (data.has("url")) {
                    return data.get("url").asText();
                }
            }
            
            logger.warn("Cannot find image URL or task_id in API response: {}", apiResponse);
            return null;
            
        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing cartoon API response", e);
            throw new BailianApiException("解析API响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 轮询异步任务结果
     */
    private String pollTaskResult(String taskId) {
        try {
            String pollUrl = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;
            int maxAttempts = 30; // 最多轮询30次
            int interval = 2000; // 每次间隔2秒
            
            for (int i = 0; i < maxAttempts; i++) {
                Thread.sleep(interval);
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + cartoonApiProperties.getApiKey());
                
                HttpEntity<?> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        pollUrl,
                        HttpMethod.GET,
                        requestEntity,
                        String.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    
                    // 检查任务状态
                    if (jsonNode.has("output")) {
                        JsonNode output = jsonNode.get("output");
                        String taskStatus = output.has("task_status") 
                            ? output.get("task_status").asText() 
                            : null;
                        
                        if ("SUCCEEDED".equals(taskStatus) || "SUCCESS".equals(taskStatus)) {
                            // 任务成功，提取图片URL
                            if (output.has("results") && output.get("results").isArray() && output.get("results").size() > 0) {
                                JsonNode firstResult = output.get("results").get(0);
                                if (firstResult.has("url")) {
                                    return firstResult.get("url").asText();
                                }
                            }
                        } else if ("FAILED".equals(taskStatus) || "FAIL".equals(taskStatus)) {
                            String errorMsg = output.has("message") 
                                ? output.get("message").asText() 
                                : "任务执行失败";
                            throw new BailianApiException("卡通图片生成任务失败: " + errorMsg);
                        }
                        // PENDING或RUNNING状态，继续轮询
                    }
                }
            }
            
            throw new BailianApiException("轮询任务超时，task_id: " + taskId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BailianApiException("轮询任务被中断", e);
        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error polling task result", e);
            throw new BailianApiException("轮询任务结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载并保存生成的卡通图片到本地和R2
     * 使用Java原生URLConnection避免RestTemplate对URL的重新编码导致OSS签名不匹配
     * 下载后上传到Cloudflare R2并生成预签名URL和二维码
     */
    private CartoonImageResult downloadAndSaveCartoonImage(String imageUrl) {
        try {
            logger.debug("Downloading cartoon image from: {}", imageUrl);
            
            // 使用Java原生URLConnection下载，避免RestTemplate对URL的重新编码
            URL url = new URL(imageUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                throw new BailianApiException("下载卡通图片失败，HTTP状态码: " + responseCode);
            }
            
            // 读取图片数据
            byte[] imageBytes;
            try (java.io.InputStream inputStream = connection.getInputStream();
                 java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                imageBytes = outputStream.toByteArray();
            } finally {
                connection.disconnect();
            }
            
            if (imageBytes == null || imageBytes.length == 0) {
                throw new BailianApiException("下载的图片数据为空");
            }
            
            // 生成文件名
            String fileName = "cartoon_" + UUID.randomUUID() + ".jpeg";
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);
            
            // 保存到本地
            Path savedPath = imageStorageService.saveBase64Image(base64Data, fileName);
            String localUrl = imageStorageService.getImageUrl(fileName);
            logger.info("Cartoon image saved locally: {}", fileName);
            
            // 上传到Cloudflare R2
            CartoonImageResult result = new CartoonImageResult();
            result.setLocalUrl(localUrl);
            result.setFileSize(imageBytes.length);
            
            try {
                String r2ObjectKey = generateR2ObjectKey();
                logger.debug("Uploading cartoon image to R2: key={}", r2ObjectKey);
                
                R2UploadService.R2ObjectInfo r2Info = r2UploadService.uploadFromBase64(
                        base64Data, r2ObjectKey, "image/jpeg");
                
                result.setR2ObjectKey(r2ObjectKey);
                
                // 生成预签名URL（600秒有效期）
                String presignedUrl = r2UploadService.getPresignedUrl(r2ObjectKey, 
                        r2Properties.getPresignedUrlExpiration());
                result.setPresignedUrl(presignedUrl);
                
                logger.info("R2 presigned URL generated: expires in {} seconds", 
                        r2Properties.getPresignedUrlExpiration());
                
                // 生成二维码（编码presigned URL）
                try {
                    String qrCodeBase64 = qrCodeService.generateQrCodeBase64(presignedUrl, 400, 400);
                    result.setQrCodeBase64(qrCodeBase64);
                    logger.info("QR code generated successfully");
                } catch (Exception e) {
                    logger.warn("Failed to generate QR code, continuing without it", e);
                }
                
            } catch (FileStorageException e) {
                logger.warn("R2 upload failed, but continuing with local storage: {}", e.getMessage());
            } catch (Exception e) {
                logger.warn("R2 operations failed, but continuing with local storage: {}", e.getMessage());
            }
            
            return result;
            
        } catch (java.net.MalformedURLException e) {
            logger.error("Invalid image URL: {}", imageUrl, e);
            throw new BailianApiException("无效的图片URL: " + imageUrl, e);
        } catch (java.io.IOException e) {
            logger.error("Error downloading image from OSS", e);
            throw new BailianApiException("下载卡通图片失败: " + e.getMessage(), e);
        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error downloading and saving cartoon image", e);
            throw new BailianApiException("保存卡通图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成R2对象key，使用UUID加时间戳避免可猜测
     */
    private String generateR2ObjectKey() {
        String uuid = UUID.randomUUID().toString();
        long timestamp = Instant.now().toEpochMilli();
        return String.format("cartoon/%s-%d.jpeg", uuid, timestamp);
    }

    /**
     * 从图片URL提取本地文件路径
     */
    private Path extractLocalPathFromUrl(String imageUrl) {
        try {
            // 如果URL包含baseUrl，提取文件名
            String baseUrl = uploadProperties.getBaseUrl();
            if (imageUrl.startsWith(baseUrl)) {
                String fileName = imageUrl.substring(baseUrl.length());
                Path uploadPath = Paths.get(uploadProperties.getPath());
                return uploadPath.resolve(fileName);
            }
            
            // 如果是完整URL，尝试解析
            URL url = new URL(imageUrl);
            String path = url.getPath();
            if (path.startsWith("/upload/")) {
                String fileName = path.substring("/upload/".length());
                Path uploadPath = Paths.get(uploadProperties.getPath());
                return uploadPath.resolve(fileName);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting local path from URL: {}", imageUrl, e);
            return null;
        }
    }
}

