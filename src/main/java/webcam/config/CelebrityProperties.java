package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 中国明星照片配置
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "celebrity")
public class CelebrityProperties {

    private List<String> malePhotos = new ArrayList<>();
    private List<String> femalePhotos = new ArrayList<>();

    public CelebrityProperties() {
        initializeMalePhotos();
        initializeFemalePhotos();
    }

    private void initializeMalePhotos() {
        // 中国男明星照片 - 使用本地图片，更可靠（避免外部CDN失效或防盗链问题）
        // 备注：外部图床URL可能随时失效，导致明星照片无法显示
        // 使用本地图片：/male/1.png ~ /male/10.png
        malePhotos.add("/male/1.png"); // 男性明星 1
        malePhotos.add("/male/2.png"); // 男性明星 2
        malePhotos.add("/male/3.png"); // 男性明星 3
        malePhotos.add("/male/4.png"); // 男性明星 4
        malePhotos.add("/male/5.png"); // 男性明星 5
        malePhotos.add("/male/6.png"); // 男性明星 6
        malePhotos.add("/male/7.png"); // 男性明星 7
        malePhotos.add("/male/8.png"); // 男性明星 8
        malePhotos.add("/male/9.png"); // 男性明星 9
        malePhotos.add("/male/10.png"); // 男性明星 10
    }

    private void initializeFemalePhotos() {
        // 中国女明星照片 - 使用本地图片，更可靠（避免外部CDN失效或防盗链问题）
        // 备注：外部图床URL可能随时失效，导致明星照片无法显示
        // 使用本地图片：/female/1.png ~ /female/10.png
        femalePhotos.add("/female/1.png"); // 女性明星 1
        femalePhotos.add("/female/2.png"); // 女性明星 2
        femalePhotos.add("/female/3.png"); // 女性明星 3
        femalePhotos.add("/female/4.png"); // 女性明星 4
        femalePhotos.add("/female/5.png"); // 女性明星 5
        femalePhotos.add("/female/6.png"); // 女性明星 6
        femalePhotos.add("/female/7.png"); // 女性明星 7
        femalePhotos.add("/female/8.png"); // 女性明星 8
        femalePhotos.add("/female/9.png"); // 女性明星 9
        femalePhotos.add("/female/10.png"); // 女性明星 10
    }

    public List<String> getMalePhotos() {
        return malePhotos;
    }

    public void setMalePhotos(List<String> malePhotos) {
        this.malePhotos = malePhotos;
    }

    public List<String> getFemalePhotos() {
        return femalePhotos;
    }

    public void setFemalePhotos(List<String> femalePhotos) {
        this.femalePhotos = femalePhotos;
    }
}
