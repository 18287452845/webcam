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
        // 中国男明星照片URL列表 - 使用高质量CDN图片
        malePhotos.add("https://img1.baidu.com/it/u=2839653127,3970469241&fm=253&fmt=auto&app=138&f=JPEG"); // 吴京
        malePhotos.add("https://img2.baidu.com/it/u=3716951733,2758164488&fm=253&fmt=auto&app=138&f=JPEG"); // 胡歌
        malePhotos.add("https://img1.baidu.com/it/u=1960452892,2313764692&fm=253&fmt=auto&app=138&f=JPEG"); // 黄晓明
        malePhotos.add("https://img2.baidu.com/it/u=3044269366,2657747430&fm=253&fmt=auto&app=138&f=JPEG"); // 陈坤
        malePhotos.add("https://img1.baidu.com/it/u=2715387672,3978876918&fm=253&fmt=auto&app=138&f=JPEG"); // 邓超
        malePhotos.add("https://img0.baidu.com/it/u=2856837948,3390800171&fm=253&fmt=auto&app=138&f=JPEG"); // 刘德华
        malePhotos.add("https://img0.baidu.com/it/u=2674613357,1066632851&fm=253&fmt=auto&app=138&f=JPEG"); // 张国荣
        malePhotos.add("https://img2.baidu.com/it/u=2944459882,2923148674&fm=253&fmt=auto&app=138&f=JPEG"); // 周杰伦
        malePhotos.add("https://img1.baidu.com/it/u=3157298651,2648316863&fm=253&fmt=auto&app=138&f=JPEG"); // 王力宏
        malePhotos.add("https://img0.baidu.com/it/u=3988550846,2749428658&fm=253&fmt=auto&app=138&f=JPEG"); // 李易峰
    }

    private void initializeFemalePhotos() {
        // 中国女明星照片URL列表 - 使用高质量CDN图片
        femalePhotos.add("https://img0.baidu.com/it/u=3775428652,2868156394&fm=253&fmt=auto&app=138&f=JPEG"); // 刘亦菲
        femalePhotos.add("https://img2.baidu.com/it/u=4164373443,1565726825&fm=253&fmt=auto&app=138&f=JPEG"); // 杨幂
        femalePhotos.add("https://img1.baidu.com/it/u=2867906828,3712538536&fm=253&fmt=auto&app=138&f=JPEG"); // 赵丽颖
        femalePhotos.add("https://img0.baidu.com/it/u=1729737277,1684224466&fm=253&fmt=auto&app=138&f=JPEG"); // 迪丽热巴
        femalePhotos.add("https://img1.baidu.com/it/u=2635390837,3524459438&fm=253&fmt=auto&app=138&f=JPEG"); // 唐嫣
        femalePhotos.add("https://img2.baidu.com/it/u=3947572855,2932149952&fm=253&fmt=auto&app=138&f=JPEG"); // 林志玲
        femalePhotos.add("https://img0.baidu.com/it/u=1892845689,3257225924&fm=253&fmt=auto&app=138&f=JPEG"); // 范冰冰
        femalePhotos.add("https://img1.baidu.com/it/u=4192817161,3548876929&fm=253&fmt=auto&app=138&f=JPEG"); // 章子怡
        femalePhotos.add("https://img2.baidu.com/it/u=2637586634,2746853274&fm=253&fmt=auto&app=138&f=JPEG"); // 舒淇
        femalePhotos.add("https://img0.baidu.com/it/u=1546721785,3842654827&fm=253&fmt=auto&app=138&f=JPEG"); // 周迅
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
