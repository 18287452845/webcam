package webcam.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import webcam.service.FaceRecognitionService;
import webcam.service.ImageStorageService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebcamController单元测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@WebMvcTest(WebcamController.class)
class WebcamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageStorageService imageStorageService;

    @MockBean
    private FaceRecognitionService faceRecognitionService;

    private String validBase64Image;

    @BeforeEach
    void setUp() {
        // 创建一个有效的Base64图像数据
        byte[] imageBytes = new byte[1000]; // 模拟图像数据
        validBase64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    @Test
    void testProcessImage_Success() throws Exception {
        // 准备测试数据
        String base64Data = Base64.getEncoder().encodeToString(new byte[1000]);
        Path mockPath = Paths.get("test.jpeg");

        Map<String, Object> faceAttributes = new HashMap<>();
        faceAttributes.put("gender", "男性");
        faceAttributes.put("age", 25);
        faceAttributes.put("smile", "微笑");
        faceAttributes.put("eyestatus", "不带眼镜并且睁眼");
        faceAttributes.put("praise", "你的笑容真好看，给人一种很舒服的感觉！");
        faceAttributes.put("healthAnalysis", "基于人脸特征的健康分析内容");

        // 配置Mock行为
        when(imageStorageService.extractBase64Data(anyString())).thenReturn(base64Data);
        when(imageStorageService.saveBase64Image(anyString(), anyString())).thenReturn(mockPath);
        when(imageStorageService.getImageUrl(anyString())).thenReturn("http://localhost:8080/upload/test.jpeg");
        when(faceRecognitionService.detectFaceAttributes(any(Path.class))).thenReturn(faceAttributes);

        // 执行测试
        mockMvc.perform(post("/webcam")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("image", validBase64Image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("1"))
                .andExpect(jsonPath("$.msg.gender").value("男性"))
                .andExpect(jsonPath("$.msg.age").value(25))
                .andExpect(jsonPath("$.msg.smile").value("微笑"))
                .andExpect(jsonPath("$.msg.praise").value("你的笑容真好看，给人一种很舒服的感觉！"));
    }

    @Test
    void testProcessImage_InvalidData() throws Exception {
        // 配置Mock抛出异常
        when(imageStorageService.extractBase64Data(anyString())).thenReturn("invalid");
        when(imageStorageService.saveBase64Image(anyString(), anyString()))
                .thenThrow(new webcam.exception.ImageProcessingException("无效的Base64数据"));

        // 执行测试 - 应该返回400错误
        mockMvc.perform(post("/webcam")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("image", "invalid_data"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("0"));
    }
}
