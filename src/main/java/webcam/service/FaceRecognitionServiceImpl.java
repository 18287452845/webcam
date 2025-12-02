package webcam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import webcam.MapUtil;
import webcam.config.FacePlusPlusProperties;
import webcam.exception.FacePlusPlusApiException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别服务实现
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);

    private final FacePlusPlusProperties facePlusPlusProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FaceRecognitionServiceImpl(FacePlusPlusProperties facePlusPlusProperties,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.facePlusPlusProperties = facePlusPlusProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> detectFaceAttributes(Path imagePath) {
        try {
            // 调用Face++ API
            String apiResponse = callFacePlusPlusAPI(imagePath);
            logger.debug("Face++ API response: {}", apiResponse);

            // 解析响应
            return parseApiResponse(apiResponse);

        } catch (Exception e) {
            logger.error("Error detecting face attributes for image: {}", imagePath, e);
            throw new FacePlusPlusApiException("人脸检测失败", e);
        }
    }

    @Override
    public Map<String, Object> parseApiResponse(String apiResponse) {
        try {
            JsonNode apiJson = objectMapper.readTree(apiResponse);
            Map<String, Object> resultData = new HashMap<>();

            // 检查是否检测到人脸
            JsonNode facesArray = apiJson.get("faces");
            if (facesArray != null && facesArray.isArray() && facesArray.size() > 0) {
                JsonNode firstFace = facesArray.get(0);
                String faceToken = firstFace.get("face_token").asText();
                resultData.put("faceToken", faceToken);

                JsonNode attributes = firstFace.get("attributes");
                if (attributes != null) {
                    // 处理眼睛状态
                    parseEyeStatus(attributes, resultData);

                    // 处理笑容
                    parseSmile(attributes, resultData);

                    // 处理性别
                    parseGender(attributes, resultData);

                    // 处理年龄
                    parseAge(attributes, resultData);
                }
            } else {
                logger.info("No face detected in image");
            }

            return resultData;

        } catch (Exception e) {
            logger.error("Error parsing Face++ API response", e);
            throw new FacePlusPlusApiException("解析API响应失败", e);
        }
    }

    /**
     * 调用Face++ API进行人脸检测
     * 
     * @param imagePath 图像文件路径
     * @return API响应字符串
     */
    private String callFacePlusPlusAPI(Path imagePath) {
        try {
            // 构建multipart请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("api_key", facePlusPlusProperties.getKey());
            body.add("api_secret", facePlusPlusProperties.getSecret());
            body.add("image_file", new org.springframework.core.io.FileSystemResource(imagePath.toFile()));
            body.add("return_attributes", facePlusPlusProperties.getReturnAttributes());
            body.add("return_landmark", facePlusPlusProperties.getReturnLandmark());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    facePlusPlusProperties.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error calling Face++ API", e);
            throw new FacePlusPlusApiException("调用Face++ API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析眼睛状态
     */
    private void parseEyeStatus(JsonNode attributes, Map<String, Object> resultData) {
        JsonNode eyestatus = attributes.get("eyestatus");
        if (eyestatus != null) {
            Map<String, Double> eyeStatusMap = extractEyeStatus(eyestatus);
            resultData.put("eyestatus", MapUtil.glass(eyeStatusMap));
        }
    }

    /**
     * 解析笑容
     */
    private void parseSmile(JsonNode attributes, Map<String, Object> resultData) {
        JsonNode smile = attributes.get("smile");
        if (smile != null) {
            double value = smile.get("value").asDouble();
            double threshold = smile.get("threshold").asDouble();
            resultData.put("smile", MapUtil.smiling(value, threshold));
            logger.debug("Smile value: {}, threshold: {}, isSmiling: {}",
                    value, threshold, value > threshold);
        }
    }

    /**
     * 解析性别
     */
    private void parseGender(JsonNode attributes, Map<String, Object> resultData) {
        JsonNode gender = attributes.get("gender");
        if (gender != null) {
            String genderValue = gender.get("value").asText();
            resultData.put("gender", MapUtil.gender(genderValue));
        }
    }

    /**
     * 解析年龄
     */
    private void parseAge(JsonNode attributes, Map<String, Object> resultData) {
        JsonNode age = attributes.get("age");
        if (age != null) {
            resultData.put("age", age.get("value").asInt());
        }
    }

    /**
     * 提取眼睛状态信息
     * 
     * @param eyestatus 眼睛状态JSON节点
     * @return 眼睛状态Map
     */
    private Map<String, Double> extractEyeStatus(JsonNode eyestatus) {
        Map<String, Double> eyeStatusMap = new HashMap<>();

        JsonNode leftEye = eyestatus.get("left_eye_status");
        JsonNode rightEye = eyestatus.get("right_eye_status");

        if (leftEye != null) {
            eyeStatusMap.put("leftNormalOpen", leftEye.get("normal_glass_eye_open").asDouble());
            eyeStatusMap.put("leftNoClose", leftEye.get("no_glass_eye_close").asDouble());
            eyeStatusMap.put("leftOcclusion", leftEye.get("occlusion").asDouble());
            eyeStatusMap.put("leftNoOpen", leftEye.get("no_glass_eye_open").asDouble());
            eyeStatusMap.put("leftNormalClose", leftEye.get("normal_glass_eye_close").asDouble());
            eyeStatusMap.put("leftDark", leftEye.get("dark_glasses").asDouble());
        }

        if (rightEye != null) {
            eyeStatusMap.put("rightNormalOpen", rightEye.get("normal_glass_eye_open").asDouble());
            eyeStatusMap.put("rightNoClose", rightEye.get("no_glass_eye_close").asDouble());
            eyeStatusMap.put("rightOcclusion", rightEye.get("occlusion").asDouble());
            eyeStatusMap.put("rightNoOpen", rightEye.get("no_glass_eye_open").asDouble());
            eyeStatusMap.put("rightNormalClose", rightEye.get("normal_glass_eye_close").asDouble());
            eyeStatusMap.put("rightDark", rightEye.get("dark_glasses").asDouble());
        }

        return eyeStatusMap;
    }
}
