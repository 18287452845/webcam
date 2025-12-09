package webcam;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 工具类
 * 提供人脸识别结果的数据映射和转换功能
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public class MapUtil {

    /**
     * 根据眼睛状态判断眼镜佩戴情况
     * 
     * @param eye 眼睛状态Map，包含左右眼的各种状态值
     * @return 眼镜状态描述
     */
    public static String glass(Map<String, Double> eye) {
        if (eye == null || eye.isEmpty()) {
            return "无法识别";
        }
        
        if (hasHighConfidence(eye, "leftDark") || hasHighConfidence(eye, "rightDark")) {
            return "带墨镜";
        }
        if (hasHighConfidence(eye, "leftNormalOpen") || hasHighConfidence(eye, "rightNormalOpen")) {
            return "带普通眼镜并且睁眼";
        }
        if (hasHighConfidence(eye, "leftNormalClose") || hasHighConfidence(eye, "rightNormalClose")) {
            return "带普通眼镜并且闭眼";
        }
        if (hasHighConfidence(eye, "leftNoOpen") || hasHighConfidence(eye, "rightNoOpen")) {
            return "不带眼镜并且睁眼";
        }
        if (hasHighConfidence(eye, "leftNoClose") || hasHighConfidence(eye, "rightNoClose")) {
            return "不带眼镜并且闭眼";
        }
        if (hasHighConfidence(eye, "leftOcclusion") || hasHighConfidence(eye, "rightOcclusion")) {
            return "眼镜前面有遮挡物";
        }
        
        return "无法识别";
    }
    
    /**
     * 判断指定键的置信度是否达到阈值
     */
    private static boolean hasHighConfidence(Map<String, Double> eye, String key) {
        return eye.getOrDefault(key, 0.0) >= 90.0;
    }
    
    /**
     * 根据笑容值判断笑容状态
     * 
     * @param value 笑容值
     * @param threshold 阈值
     * @return 笑容状态描述
     */
    public static String smiling(double value, double threshold) {
        if (value > threshold + 30) {
            return "笑的灿烂";
        } else if (value > threshold) {
            return "微笑";
        } else {
            return "不笑";
        }
    }
    
    /**
     * 转换性别值
     * 
     * @param value 性别值（Male/Female/男性/女性）
     * @return 中文性别描述
     */
    public static String gender(String value) {
        if (value == null) {
            return "未知";
        }
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains("male") || lowerValue.contains("男")) {
            return "男性";
        } else if (lowerValue.contains("female") || lowerValue.contains("女")) {
            return "女性";
        }
        return "未知";
    }
    
    /**
     * 根据索引获取匹配描述
     * 
     * @param index 描述索引（1-7）
     * @return 匹配描述文本
     */
    public static String pDesc(int index) {
        return switch (index) {
            case 1 -> "不会有震天动地狂风暴雨般的恋情，每每是细水长流，有如磁铁般精密符合的满分组合。跟你一样，非常珍视完全拥有对方。为了储备与家庭快乐，你们两人可取得相互和谐，你的俏丽与可敬，使摩羯放心。相互对恋爱的忠诚度划一，使你们的爱情出息宏大。可从你身上发明温柔与俏皮的一壁，而你也能认同摩羯的真诚与可靠";
            case 2 -> "寻求单纯爱情的你，与TA乐于办事他人的天性极为得当。信守双方的约定，使你放心不己。但是，你也必须克制出现孩子般的嫉妨心与占据欲。若能以成熟人的面貌与之来往的话，干系将会一帆风顺";
            case 3 -> "很看重宁静感，也有储备的风俗。不会方便暴露真情。若到了可以信任的地步，你将是他唯一值得流露心事的人。同时也向往像你这种范例的人。一旦来往下去，要是没有非常紧张的问题，可望步入完婚礼堂";
            case 4 -> "很容易孕育产生共鸣共振的以为，遇到TA时，将会使你们的热度一下子升到最高点，是一见钟情式、康健开朗的组合，高兴永世存在。你们两个在一起的话，更像两个玩疯的小孩，不但会利用周遭的事物来玩的痛快，你们的互动干系也像是一出无比浮夸的戏剧";
            case 5 -> "两人的意见、观念、行为都大致相同，也很容易产生共鸣；火相星座两只白羊一旦来电，就会很速度的浓烈的发展，因为TA热情如火，只要是喜欢对方就会奋不顾身。还有，您们的爱恋像清咖一样浓";
            case 6, 7 -> "在初次见面的时候，十成八九都会产生剧烈的火花，像磁铁一样互相吸引、一见钟情的指数很高噢，同时外向的性格让你们的恋情发展速度迅猛。两人的性格都较为接近，所以在一起绝对是天生一对，恩爱羡煞旁人";
            default -> "匹配成功！";
        };
    }
}

