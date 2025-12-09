package webcam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import webcam.MapUtil;
import webcam.config.BailianApiProperties;
import webcam.exception.BailianApiException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务实现
 * 使用阿里云百炼API进行人脸识别和健康分析
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);

    private final BailianApiProperties bailianApiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FaceRecognitionServiceImpl(BailianApiProperties bailianApiProperties,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.bailianApiProperties = bailianApiProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> detectFaceAttributes(Path imagePath) {
        try {
            // 调用百炼API
            String apiResponse = callBailianAPI(imagePath);
            logger.debug("百炼API response: {}", apiResponse);

            // 解析响应
            return parseApiResponse(apiResponse);

        } catch (Exception e) {
            logger.error("Error detecting face attributes for image: {}", imagePath, e);
            throw new BailianApiException("人脸检测失败", e);
        }
    }

    @Override
    public Map<String, Object> parseApiResponse(String apiResponse) {
        try {
            JsonNode apiJson = objectMapper.readTree(apiResponse);
            Map<String, Object> resultData = new HashMap<>();

            // 检查API响应是否成功（qwen3-vl-plus使用OpenAI兼容格式）
            if (apiJson.has("error")) {
                JsonNode error = apiJson.get("error");
                String errorMsg = error.has("message") ? error.get("message").asText() : "API调用失败";
                logger.error("百炼API返回错误: {}", errorMsg);
                throw new BailianApiException("百炼API调用失败: " + errorMsg);
            }

            // 提取模型返回的文本内容
            String content = extractContent(apiJson);
            if (content == null || content.trim().isEmpty()) {
                logger.info("No content returned from API");
                return resultData;
            }

            // 解析内容，提取结构化信息
            parseContent(content, resultData);
            
            // 确保健康分析和夸奖内容存在
            if (!resultData.containsKey("healthAnalysis")) {
                String healthAnalysis = limitHealthAnalysisLength(content, bailianApiProperties.getMaxHealthAnalysisLength());
                resultData.put("healthAnalysis", healthAnalysis);
            }
            
            // 如果没有praise，使用默认值
            if (!resultData.containsKey("praise")) {
                resultData.put("praise", "你真棒！");
            }

            return resultData;

        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing 百炼API response", e);
            throw new BailianApiException("解析API响应失败", e);
        }
    }

    /**
     * 调用百炼API进行人脸检测和健康分析
     * 
     * @param imagePath 图像文件路径
     * @return API响应字符串
     */
    private String callBailianAPI(Path imagePath) {
        try {
            // 读取图片并转换为Base64
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 构建请求体（按照qwen3-vl-plus API格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", bailianApiProperties.getModel());
            
            // 构建消息内容
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            // 构建内容数组，包含图片和文本（按照API文档格式）
            List<Map<String, Object>> contentList = new ArrayList<>();
            
            // 图片内容
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            contentList.add(imageContent);
            
            // 文本内容
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", buildPrompt());
            contentList.add(textContent);
            
            message.put("content", contentList);
            
            // messages直接在顶层，不在input中
            List<Map<String, Object>> messagesList = new ArrayList<>();
            messagesList.add(message);
            requestBody.put("messages", messagesList);
            
            // 参数直接在顶层
            requestBody.put("temperature", bailianApiProperties.getTemperature());
            requestBody.put("max_tokens", bailianApiProperties.getMaxTokens());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + bailianApiProperties.getApiKey());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    bailianApiProperties.getEndpoint(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new BailianApiException("百炼API调用失败，状态码: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (BailianApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error calling 百炼API", e);
            throw new BailianApiException("调用百炼API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建提示词，要求模型识别人脸特征、分析健康情况并生成夸奖内容
     */
    private String buildPrompt() {
        return "请仔细分析这张人脸照片，完成以下任务：\n" +
               "1. 识别人脸特征：\n" +
               "   - 性别（男性/女性）\n" +
               "   - 估计年龄（整数）\n" +
               "   - 表情状态（是否微笑）\n" +
               "   - 眼睛状态（是否戴眼镜、眼睛是否睁开等）\n" +
               "   - 气色（红润/苍白/暗沉等）\n" +
               "   - 精神状态（精神饱满/疲惫等）\n" +
               "\n" +
               "2. 健康分析：\n" +
               "   - 基于观察到的人脸特征，分析可能的健康情况\n" +
               "   - 提供健康建议（如需要）\n" +
               "\n" +
               "3. 生成夸奖内容：\n" +
               "   - 基于观察到的人脸特征，生成一段60-80字的个性化夸奖内容\n" +
               "   - 夸奖要真诚、自然、有针对性\n" +
               "\n" +
               "请以JSON格式返回结果，格式如下：\n" +
               "{\n" +
               "  \"gender\": \"男性或女性\",\n" +
               "  \"age\": 年龄数字,\n" +
               "  \"smile\": \"是或否\",\n" +
               "  \"eyestatus\": \"眼睛状态描述\",\n" +
               "  \"complexion\": \"气色描述\",\n" +
               "  \"spirit\": \"精神状态描述\",\n" +
               "  \"healthAnalysis\": \"健康分析和建议（不超过600字）\",\n" +
               "  \"praise\": \"个性化夸奖内容（60-80字）\"\n" +
               "}";
    }

    /**
     * 从API响应中提取内容（OpenAI兼容格式）
     */
    private String extractContent(JsonNode apiJson) {
        try {
            // qwen3-vl-plus使用OpenAI兼容格式，choices直接在顶层
            if (apiJson.has("choices") && apiJson.get("choices").isArray() && apiJson.get("choices").size() > 0) {
                JsonNode firstChoice = apiJson.get("choices").get(0);
                if (firstChoice.has("message")) {
                    JsonNode message = firstChoice.get("message");
                    if (message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error extracting content from API response", e);
            return null;
        }
    }

    /**
     * 限制健康分析内容长度
     */
    private String limitHealthAnalysisLength(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        // 如果内容超过限制，截取前maxLength个字符
        if (content.length() > maxLength) {
            return content.substring(0, maxLength) + "...";
        }
        return content;
    }

    /**
     * 解析API返回的内容，提取结构化信息
     */
    private void parseContent(String content, Map<String, Object> resultData) {
        try {
            // 尝试从内容中提取JSON
            String jsonStr = extractJsonFromContent(content);
            if (jsonStr != null) {
                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                
                // 提取性别
                if (jsonNode.has("gender")) {
                    String genderValue = jsonNode.get("gender").asText();
                    resultData.put("gender", MapUtil.gender(genderValue));
                }
                
                // 提取年龄
                if (jsonNode.has("age")) {
                    resultData.put("age", jsonNode.get("age").asInt());
                }
                
                // 提取笑容
                if (jsonNode.has("smile")) {
                    String smileValue = jsonNode.get("smile").asText();
                    resultData.put("smile", "是".equals(smileValue) || "yes".equalsIgnoreCase(smileValue) ? "是" : "否");
                }
                
                // 提取眼睛状态
                if (jsonNode.has("eyestatus")) {
                    resultData.put("eyestatus", jsonNode.get("eyestatus").asText());
                }
                
                // 提取气色
                if (jsonNode.has("complexion")) {
                    resultData.put("complexion", jsonNode.get("complexion").asText());
                }
                
                // 提取精神状态
                if (jsonNode.has("spirit")) {
                    resultData.put("spirit", jsonNode.get("spirit").asText());
                }
                
                // 提取健康分析（已在parseApiResponse中处理）
                if (jsonNode.has("healthAnalysis")) {
                    String healthAnalysis = jsonNode.get("healthAnalysis").asText();
                    resultData.put("healthAnalysis", limitHealthAnalysisLength(healthAnalysis, bailianApiProperties.getMaxHealthAnalysisLength()));
                }
                
                // 提取夸奖内容
                if (jsonNode.has("praise")) {
                    resultData.put("praise", jsonNode.get("praise").asText());
                }
            } else {
                // 如果无法提取JSON，尝试从文本中提取关键信息
                extractFromText(content, resultData);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse content as JSON, trying text extraction", e);
            extractFromText(content, resultData);
        }
    }

    /**
     * 从内容中提取JSON字符串
     */
    private String extractJsonFromContent(String content) {
        if (content == null) {
            return null;
        }
        
        // 查找第一个 { 和最后一个 }
        int startIdx = content.indexOf('{');
        int endIdx = content.lastIndexOf('}');
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return content.substring(startIdx, endIdx + 1);
        }
        
        return null;
    }

    /**
     * 从文本中提取关键信息（备用方法）
     */
    private void extractFromText(String text, Map<String, Object> resultData) {
        // 简单的文本匹配提取
        if (text.contains("男性") || text.contains("男")) {
            resultData.put("gender", "男性");
        } else if (text.contains("女性") || text.contains("女")) {
            resultData.put("gender", "女性");
        }
        
        // 提取年龄（查找数字）
        java.util.regex.Pattern agePattern = java.util.regex.Pattern.compile("(\\d+)岁|年龄[：:]\\s*(\\d+)");
        java.util.regex.Matcher matcher = agePattern.matcher(text);
        if (matcher.find()) {
            try {
                int age = Integer.parseInt(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
                resultData.put("age", age);
            } catch (NumberFormatException e) {
                // 忽略
            }
        }
        
        // 提取笑容
        if (text.contains("微笑") || text.contains("笑容") || text.contains("笑")) {
            resultData.put("smile", "是");
        } else {
            resultData.put("smile", "否");
        }
    }
}
