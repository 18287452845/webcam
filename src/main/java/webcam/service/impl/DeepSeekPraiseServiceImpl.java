package webcam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import webcam.config.DeepSeekProperties;
import webcam.service.DeepSeekPraiseService;

import java.util.Map;

/**
 * DeepSeek AI夸奖服务实现
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class DeepSeekPraiseServiceImpl implements DeepSeekPraiseService {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekPraiseServiceImpl.class);

    private final DeepSeekProperties deepSeekProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeepSeekPraiseServiceImpl(DeepSeekProperties deepSeekProperties,
                                     RestTemplate restTemplate,
                                     ObjectMapper objectMapper) {
        this.deepSeekProperties = deepSeekProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generatePraise(Map<String, Object> faceAttributes) {
        try {
            if (faceAttributes == null || faceAttributes.isEmpty()) {
                logger.warn("No face attributes provided for praise generation");
                return getFallbackPraise();
            }

            String prompt = buildPrompt(faceAttributes);
            logger.debug("Generated prompt: {}", prompt);

            String response = callDeepSeekAPI(prompt);
            String praise = extractPraise(response);

            if (praise == null || praise.trim().isEmpty()) {
                logger.warn("Empty praise received from DeepSeek API, using fallback");
                return getFallbackPraise();
            }

            logger.info("Successfully generated praise: {}", praise);
            return praise;

        } catch (Exception e) {
            logger.error("Error generating praise with DeepSeek API", e);
            return getFallbackPraise();
        }
    }

    private String buildPrompt(Map<String, Object> faceAttributes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下人脸属性，生成一段真诚、温暖的夸奖话语（60-80字）：\n");

        if (faceAttributes.containsKey("gender")) {
            prompt.append("性别：").append(faceAttributes.get("gender")).append("\n");
        }
        if (faceAttributes.containsKey("age")) {
            prompt.append("年龄：").append(faceAttributes.get("age")).append("岁\n");
        }
        if (faceAttributes.containsKey("smile")) {
            prompt.append("笑容：").append(faceAttributes.get("smile")).append("\n");
        }
        if (faceAttributes.containsKey("eyestatus")) {
            prompt.append("眼镜：").append(faceAttributes.get("eyestatus")).append("\n");
        }

        prompt.append("\n要求：\n");
        prompt.append("1. 语言亲切、自然，避免过度夸张\n");
        prompt.append("2. 结合具体属性进行夸奖\n");
        prompt.append("3. 60-80字左右\n");
        prompt.append("4. 不要使用「您」，使用「你」即可\n");
        prompt.append("5. 直接输出夸奖内容，不要有任何前缀或解释\n");

        return prompt.toString();
    }

    private String callDeepSeekAPI(String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", deepSeekProperties.getModel());
            requestBody.put("temperature", deepSeekProperties.getTemperature());
            requestBody.put("max_tokens", deepSeekProperties.getMaxTokens());

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekProperties.getKey());

            HttpEntity<String> requestEntity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekProperties.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error calling DeepSeek API", e);
            throw new RuntimeException("调用DeepSeek API失败: " + e.getMessage(), e);
        }
    }

    private String extractPraise(String apiResponse) {
        try {
            JsonNode responseJson = objectMapper.readTree(apiResponse);
            JsonNode choices = responseJson.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText().trim();
                    }
                }
            }

            logger.warn("Could not extract praise from API response");
            return null;

        } catch (Exception e) {
            logger.error("Error parsing DeepSeek API response", e);
            return null;
        }
    }

    private String getFallbackPraise() {
        String[] fallbackPraises = {
                "你的气质真好，笑容很有感染力，给人一种很舒服的感觉。无论是外在形象还是内在气质，都让人印象深刻，相信你在生活中一定是个很有魅力的人！",
                "你看起来真的很棒！面部轮廓分明，五官协调，整体给人一种精神饱满的感觉。特别是你的笑容，温暖又自然，一定能给身边的人带来很多正能量。",
                "你的气质很出众，看起来是个自信又友善的人。你的面部特征很有特点，给人留下深刻印象。保持这份自信和笑容，你一定会越来越有魅力！",
                "你的外表真的很有亲和力，笑起来特别好看！整体气质给人一种温暖、舒适的感觉。相信在生活中，你一定是个很受欢迎的人，继续保持这份美好！",
                "你看上去精神状态很好，气质优雅大方。你的笑容很真诚，眼神中透露出自信，这些都是非常加分的特质。相信你在人群中一定很容易成为焦点！"
        };

        int randomIndex = (int) (Math.random() * fallbackPraises.length);
        return fallbackPraises[randomIndex];
    }
}
