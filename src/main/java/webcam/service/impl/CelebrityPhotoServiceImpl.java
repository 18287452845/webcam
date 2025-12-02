package webcam.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import webcam.config.CelebrityProperties;
import webcam.service.CelebrityPhotoService;

import java.security.SecureRandom;
import java.util.List;

/**
 * 中国明星照片服务实现
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class CelebrityPhotoServiceImpl implements CelebrityPhotoService {

    private static final Logger logger = LoggerFactory.getLogger(CelebrityPhotoServiceImpl.class);

    private final CelebrityProperties celebrityProperties;
    private final SecureRandom random = new SecureRandom();

    public CelebrityPhotoServiceImpl(CelebrityProperties celebrityProperties) {
        this.celebrityProperties = celebrityProperties;
    }

    @Override
    public String getRandomCelebrityPhoto(String gender) {
        List<String> photoList = "female".equalsIgnoreCase(gender)
                ? celebrityProperties.getFemalePhotos()
                : celebrityProperties.getMalePhotos();

        if (photoList == null || photoList.isEmpty()) {
            logger.warn("No celebrity photos configured for gender: {}", gender);
            return null;
        }

        int index = random.nextInt(photoList.size());
        return photoList.get(index);
    }
}
