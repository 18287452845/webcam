package webcam.service;

import java.nio.file.Path;
import java.util.Map;

/**
 * 人脸识别服务接口
 * 负责人脸识别相关的业务逻辑
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface FaceRecognitionService {

    /**
     * 调用阿里云百炼API进行人脸检测和健康分析
     * 
     * @param imagePath 图像文件路径
     * @return 包含人脸属性和健康分析的Map (gender, age, smile, eyestatus, healthAnalysis等)
     * @throws webcam.exception.BailianApiException 当API调用失败时
     */
    Map<String, Object> detectFaceAttributes(Path imagePath);

    /**
     * 解析阿里云百炼API的响应
     * 
     * @param apiResponse API响应的JSON字符串
     * @return 解析后的人脸属性和健康分析Map
     * @throws webcam.exception.BailianApiException 当响应解析失败时
     */
    Map<String, Object> parseApiResponse(String apiResponse);
}
