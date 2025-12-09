package webcam.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webcam.MapUtil;
import webcam.service.CartoonImageService;
import webcam.service.CelebrityPhotoService;
import webcam.exception.BailianApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 结果展示控制器
 * 处理人脸识别结果的展示
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Controller
@RequestMapping("/result")
public class ResultController {

    private static final Logger logger = LoggerFactory.getLogger(ResultController.class);
    
    private final ObjectMapper objectMapper;
    private final CartoonImageService cartoonImageService;
    private final CelebrityPhotoService celebrityPhotoService;
    private final Random random = new Random();

    @Autowired
    public ResultController(ObjectMapper objectMapper, 
                           CartoonImageService cartoonImageService,
                           CelebrityPhotoService celebrityPhotoService) {
        this.objectMapper = objectMapper;
        this.cartoonImageService = cartoonImageService;
        this.celebrityPhotoService = celebrityPhotoService;
    }

    /**
     * 处理结果展示（GET请求，用于JSP页面）
     * 
     * @param msg JSON格式的结果数据（URL编码）
     * @param model Spring MVC模型
     * @return 视图名称
     */
    @GetMapping
    public String showResult(@RequestParam(value = "msg", required = false) String msg, Model model) {
        try {
            if (msg == null || msg.trim().isEmpty()) {
                logger.warn("No result data provided");
                return "redirect:/index.html";
            }

            // 解析JSON数据（已经UTF-8编码，不需要再转换）
            JsonNode jsonObject = objectMapper.readTree(msg);
            
            // 设置模型属性
            String userImageUrl = null;
            if (jsonObject.has("img")) {
                userImageUrl = jsonObject.get("img").asText();
                model.addAttribute("img", userImageUrl);
            }
            if (jsonObject.has("faceToken")) {
                model.addAttribute("faceToken", jsonObject.get("faceToken").asText());
            }
            if (jsonObject.has("eyestatus")) {
                model.addAttribute("eyestatus", jsonObject.get("eyestatus").asText());
            }
            if (jsonObject.has("smile")) {
                model.addAttribute("smile", jsonObject.get("smile").asText());
            }
            if (jsonObject.has("gender")) {
                String gender = jsonObject.get("gender").asText();
                model.addAttribute("gender", gender);

                // 尝试生成卡通图片，失败则使用明星照片作为降级方案
                String cartoonImageUrl = generateCartoonImageWithFallback(userImageUrl, gender);
                model.addAttribute("ppei", cartoonImageUrl);
            }
            if (jsonObject.has("age")) {
                model.addAttribute("age", jsonObject.get("age").asText());
            }
            if (jsonObject.has("praise")) {
                model.addAttribute("praise", jsonObject.get("praise").asText());
            } else {
                model.addAttribute("praise", "你真棒！");
            }
            if (jsonObject.has("healthAnalysis")) {
                model.addAttribute("healthAnalysis", jsonObject.get("healthAnalysis").asText());
            }

            // 随机生成描述
            model.addAttribute("data_index", MapUtil.pDesc(random.nextInt(3) + 1));
            model.addAttribute("pdesc", MapUtil.pDesc(random.nextInt(7) + 1));

            logger.debug("Result page data prepared successfully");
            return "result"; // 对应 result.jsp 或 result.html

        } catch (Exception e) {
            logger.error("Error processing result data", e);
            return "redirect:/index.html";
        }
    }

    /**
     * 处理结果展示（POST请求，返回JSON）
     * 
     * @param resultData JSON格式的结果数据
     * @return JSON响应
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getResultJson(@RequestBody Map<String, Object> resultData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 复制原始数据
            response.putAll(resultData);

            // 根据性别选择匹配图片
            String gender = (String) resultData.get("gender");
            String userImageUrl = (String) resultData.get("img");
            if (gender != null) {
                // 尝试生成卡通图片，失败则使用明星照片作为降级方案
                String cartoonImageUrl = generateCartoonImageWithFallback(userImageUrl, gender);
                response.put("ppei", cartoonImageUrl);
            }

            // 确保夸奖内容存在
            response.putIfAbsent("praise", MapUtil.pDesc(random.nextInt(7) + 1));

            // 随机生成描述
            response.put("data_index", MapUtil.pDesc(random.nextInt(3) + 1));
            response.put("pdesc", MapUtil.pDesc(random.nextInt(7) + 1));

            logger.debug("Result JSON prepared successfully");
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing result JSON", e);
            response.put("error", "处理结果数据时发生错误: " + e.getMessage());
            return response;
        }
    }

    /**
     * 生成卡通图片，失败时使用明星照片作为降级方案
     * 
     * @param userImageUrl 用户上传的图片URL
     * @param detectedGender 百炼API检测的性别（可能为"男性/女性"或"male/female"）
     * @return 卡通图片URL或明星照片URL（降级方案）
     */
    private String generateCartoonImageWithFallback(String userImageUrl, String detectedGender) {
        // 优先尝试生成卡通图片
        if (userImageUrl != null && !userImageUrl.isEmpty()) {
            try {
                String cartoonImageUrl = cartoonImageService.generateCartoonImageFromUrl(userImageUrl);
                if (cartoonImageUrl != null && !cartoonImageUrl.isEmpty()) {
                    logger.info("Cartoon image generated successfully: {}", cartoonImageUrl);
                    return cartoonImageUrl;
                }
            } catch (BailianApiException e) {
                logger.warn("Failed to generate cartoon image, falling back to celebrity photo: {}", e.getMessage());
            } catch (Exception e) {
                logger.warn("Unexpected error generating cartoon image, falling back to celebrity photo", e);
            }
        }
        
        // 降级方案：使用明星照片
        logger.debug("Using celebrity photo as fallback");
        return selectCelebrityPhoto(detectedGender);
    }

    /**
     * 根据百炼API检测的性别选择匹配的明星照片（降级方案）
     * 男性匹配男性明星，女性匹配女性明星
     * 使用本地图片，避免外部CDN失效或防盗链问题
     * 
     * @param detectedGender 百炼API检测的性别（可能为"男性/女性"或"male/female"）
     * @return 明星照片URL（本地路径）
     */
    private String selectCelebrityPhoto(String detectedGender) {
        String matchGender;
        
        if ("女性".equalsIgnoreCase(detectedGender) || "female".equalsIgnoreCase(detectedGender)) {
            matchGender = "female";
        } else {
            // 默认匹配男性，避免出现空值
            matchGender = "male";
        }

        // 获取明星照片URL（现在配置为本地图片，更可靠）
        String celebrityPhotoUrl = celebrityPhotoService.getRandomCelebrityPhoto(matchGender);
        
        // 如果服务返回null或空字符串（异常情况），使用降级方案
        if (celebrityPhotoUrl == null || celebrityPhotoUrl.isEmpty()) {
            int randomNum = random.nextInt(10) + 1;
            celebrityPhotoUrl = "/" + matchGender + "/" + randomNum + ".png";
            logger.warn("CelebrityPhotoService returned null/empty, using fallback: {}", celebrityPhotoUrl);
        } else {
            logger.debug("Selected celebrity photo: {}", celebrityPhotoUrl);
        }
        
        return celebrityPhotoUrl;
    }
}

