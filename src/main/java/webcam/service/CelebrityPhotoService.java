package webcam.service;

/**
 * 中国明星照片服务接口
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface CelebrityPhotoService {

    /**
     * 根据性别获取随机明星照片URL
     * 
     * @param gender 性别（male/female）
     * @return 明星照片URL
     */
    String getRandomCelebrityPhoto(String gender);
}
