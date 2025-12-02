package webcam.service;

import java.util.Map;

/**
 * DeepSeek AI夸奖服务接口
 * 根据人脸属性生成个性化的夸奖内容
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface DeepSeekPraiseService {

    /**
     * 根据人脸属性生成夸奖内容
     * 
     * @param faceAttributes 人脸属性（gender, age, smile, eyestatus等）
     * @return 夸奖文本
     */
    String generatePraise(Map<String, Object> faceAttributes);
}
