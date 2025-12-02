package webcam.util;

import org.junit.jupiter.api.Test;
import webcam.MapUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MapUtil单元测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
class MapUtilTest {

    @Test
    void testGender_Male() {
        String result = MapUtil.gender("Male");
        assertEquals("男性", result);
    }

    @Test
    void testGender_Female() {
        String result = MapUtil.gender("Female");
        assertEquals("女性", result);
    }

    @Test
    void testGender_Unknown() {
        String result = MapUtil.gender("Unknown");
        assertEquals("未知", result);
    }

    @Test
    void testSmiling_IsSmiling() {
        String result = MapUtil.smiling(80.0, 50.0);
        assertEquals("微笑", result);
    }

    @Test
    void testSmiling_NotSmiling() {
        String result = MapUtil.smiling(30.0, 50.0);
        assertEquals("不笑", result);
    }

    @Test
    void testSmiling_EdgeCase() {
        String result = MapUtil.smiling(50.0, 50.0);
        assertEquals("不笑", result);
    }

    @Test
    void testGlass_NoGlassEyesOpen() {
        Map<String, Double> eyeStatus = new HashMap<>();
        eyeStatus.put("leftNoOpen", 90.0);
        eyeStatus.put("rightNoOpen", 90.0);
        eyeStatus.put("leftNormalOpen", 5.0);
        eyeStatus.put("rightNormalOpen", 5.0);
        eyeStatus.put("leftDark", 0.0);
        eyeStatus.put("rightDark", 0.0);

        String result = MapUtil.glass(eyeStatus);
        assertEquals("不带眼镜并且睁眼", result);
    }

    @Test
    void testGlass_WithNormalGlass() {
        Map<String, Double> eyeStatus = new HashMap<>();
        eyeStatus.put("leftNoOpen", 5.0);
        eyeStatus.put("rightNoOpen", 5.0);
        eyeStatus.put("leftNormalOpen", 90.0);
        eyeStatus.put("rightNormalOpen", 90.0);
        eyeStatus.put("leftDark", 0.0);
        eyeStatus.put("rightDark", 0.0);

        String result = MapUtil.glass(eyeStatus);
        assertEquals("带普通眼镜并且睁眼", result);
    }

    @Test
    void testGlass_WithDarkGlass() {
        Map<String, Double> eyeStatus = new HashMap<>();
        eyeStatus.put("leftNoOpen", 5.0);
        eyeStatus.put("rightNoOpen", 5.0);
        eyeStatus.put("leftNormalOpen", 5.0);
        eyeStatus.put("rightNormalOpen", 5.0);
        eyeStatus.put("leftDark", 90.0);
        eyeStatus.put("rightDark", 90.0);

        String result = MapUtil.glass(eyeStatus);
        assertEquals("带墨镜", result);
    }
}
