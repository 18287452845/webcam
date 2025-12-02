package webcam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import webcam.service.CelebrityPhotoService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResultController单元测试
 * 测试性别匹配逻辑和明星照片功能
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private CelebrityPhotoService celebrityPhotoService;

    @Mock
    private Model model;

    @InjectMocks
    private ResultController resultController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        resultController = new ResultController(objectMapper, celebrityPhotoService);
    }

    @Test
    void testShowResult_MaleUser_MatchesMaleCelebrity() throws Exception {
        // Given: Mock male celebrity photo
        when(celebrityPhotoService.getRandomCelebrityPhoto("male"))
            .thenReturn("https://example.com/male-celeb.jpg");

        String msgJson = "{\"gender\":\"男性\",\"userGender\":\"male\",\"img\":\"test.jpg\",\"age\":\"25\"}";

        // When: Show result
        String viewName = resultController.showResult(msgJson, model);

        // Then: Should match male celebrity
        assertEquals("result", viewName);
        verify(celebrityPhotoService).getRandomCelebrityPhoto("male");
        verify(model).addAttribute(eq("ppei"), eq("https://example.com/male-celeb.jpg"));
    }

    @Test
    void testShowResult_FemaleUser_MatchesFemaleCelebrity() throws Exception {
        // Given: Mock female celebrity photo
        when(celebrityPhotoService.getRandomCelebrityPhoto("female"))
            .thenReturn("https://example.com/female-celeb.jpg");

        String msgJson = "{\"gender\":\"女性\",\"userGender\":\"female\",\"img\":\"test.jpg\",\"age\":\"23\"}";

        // When: Show result
        String viewName = resultController.showResult(msgJson, model);

        // Then: Should match female celebrity
        assertEquals("result", viewName);
        verify(celebrityPhotoService).getRandomCelebrityPhoto("female");
        verify(model).addAttribute(eq("ppei"), eq("https://example.com/female-celeb.jpg"));
    }

    @Test
    void testShowResult_NoUserGender_UseDetectedGender() throws Exception {
        // Given: No user gender, use detected gender
        when(celebrityPhotoService.getRandomCelebrityPhoto("male"))
            .thenReturn("https://example.com/male-celeb.jpg");

        String msgJson = "{\"gender\":\"男性\",\"img\":\"test.jpg\",\"age\":\"30\"}";

        // When: Show result
        String viewName = resultController.showResult(msgJson, model);

        // Then: Should use detected gender
        assertEquals("result", viewName);
        verify(celebrityPhotoService).getRandomCelebrityPhoto("male");
    }

    @Test
    void testGetResultJson_MaleUser() {
        // Given: Mock male celebrity photo
        when(celebrityPhotoService.getRandomCelebrityPhoto("male"))
            .thenReturn("https://example.com/male-celeb.jpg");

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("gender", "男性");
        resultData.put("userGender", "male");
        resultData.put("img", "test.jpg");

        // When: Get result JSON
        Map<String, Object> response = resultController.getResultJson(resultData);

        // Then: Should contain male celebrity photo
        assertNotNull(response);
        assertEquals("https://example.com/male-celeb.jpg", response.get("ppei"));
        verify(celebrityPhotoService).getRandomCelebrityPhoto("male");
    }

    @Test
    void testGetResultJson_FemaleUser() {
        // Given: Mock female celebrity photo
        when(celebrityPhotoService.getRandomCelebrityPhoto("female"))
            .thenReturn("https://example.com/female-celeb.jpg");

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("gender", "女性");
        resultData.put("userGender", "female");
        resultData.put("img", "test.jpg");

        // When: Get result JSON
        Map<String, Object> response = resultController.getResultJson(resultData);

        // Then: Should contain female celebrity photo
        assertNotNull(response);
        assertEquals("https://example.com/female-celeb.jpg", response.get("ppei"));
        verify(celebrityPhotoService).getRandomCelebrityPhoto("female");
    }

    @Test
    void testShowResult_FallbackToLocalImage_WhenCelebrityPhotoFails() throws Exception {
        // Given: Celebrity photo service returns null
        when(celebrityPhotoService.getRandomCelebrityPhoto(anyString())).thenReturn(null);

        String msgJson = "{\"gender\":\"男性\",\"userGender\":\"male\",\"img\":\"test.jpg\"}";

        // When: Show result
        String viewName = resultController.showResult(msgJson, model);

        // Then: Should fallback to local image
        assertEquals("result", viewName);
        verify(model).addAttribute(eq("ppei"), contains("male/"));
        verify(model).addAttribute(eq("ppei"), endsWith(".png"));
    }
}
