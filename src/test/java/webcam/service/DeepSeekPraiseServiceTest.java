package webcam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import webcam.config.DeepSeekProperties;
import webcam.service.impl.DeepSeekPraiseServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * DeepSeekPraiseService单元测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekPraiseServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private DeepSeekProperties deepSeekProperties;

    private DeepSeekPraiseService deepSeekPraiseService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        deepSeekProperties = new DeepSeekProperties();
        deepSeekProperties.setUrl("https://api.deepseek.com/v1/chat/completions");
        deepSeekProperties.setKey("test_api_key");
        deepSeekProperties.setModel("deepseek-chat");
        deepSeekProperties.setTemperature(0.7);
        deepSeekProperties.setMaxTokens(500);

        deepSeekPraiseService = new DeepSeekPraiseServiceImpl(
                deepSeekProperties, restTemplate, objectMapper);
    }

    @Test
    void testGeneratePraise_Success() {
        // Given: Mock API response
        String mockResponse = """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "你的笑容很有感染力，给人一种很舒服的感觉。"
                            }
                        }
                    ]
                }
                """;

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, Object> faceAttributes = new HashMap<>();
        faceAttributes.put("gender", "男性");
        faceAttributes.put("age", 25);
        faceAttributes.put("smile", "微笑");

        // When: Generate praise
        String praise = deepSeekPraiseService.generatePraise(faceAttributes);

        // Then: Should return praise
        assertNotNull(praise);
        assertFalse(praise.isEmpty());
        assertTrue(praise.contains("笑容") || praise.contains("感染力"));
    }

    @Test
    void testGeneratePraise_EmptyAttributes_ReturnsFallback() {
        // Given: Empty attributes
        Map<String, Object> emptyAttributes = new HashMap<>();

        // When: Generate praise
        String praise = deepSeekPraiseService.generatePraise(emptyAttributes);

        // Then: Should return fallback praise
        assertNotNull(praise);
        assertFalse(praise.isEmpty());
    }

    @Test
    void testGeneratePraise_NullAttributes_ReturnsFallback() {
        // When: Generate praise with null
        String praise = deepSeekPraiseService.generatePraise(null);

        // Then: Should return fallback praise
        assertNotNull(praise);
        assertFalse(praise.isEmpty());
    }

    @Test
    void testGeneratePraise_APIError_ReturnsFallback() {
        // Given: Mock API throws exception
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(String.class)))
                .thenThrow(new RuntimeException("API error"));

        Map<String, Object> faceAttributes = new HashMap<>();
        faceAttributes.put("gender", "女性");
        faceAttributes.put("age", 23);

        // When: Generate praise
        String praise = deepSeekPraiseService.generatePraise(faceAttributes);

        // Then: Should return fallback praise
        assertNotNull(praise);
        assertFalse(praise.isEmpty());
    }

    @Test
    void testGeneratePraise_InvalidResponse_ReturnsFallback() {
        // Given: Invalid JSON response
        String invalidResponse = "{invalid json}";

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(invalidResponse, HttpStatus.OK));

        Map<String, Object> faceAttributes = new HashMap<>();
        faceAttributes.put("gender", "男性");

        // When: Generate praise
        String praise = deepSeekPraiseService.generatePraise(faceAttributes);

        // Then: Should return fallback praise
        assertNotNull(praise);
        assertFalse(praise.isEmpty());
    }
}
