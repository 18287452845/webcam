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
import webcam.service.CelebrityPhotoService;

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
	private final CelebrityPhotoService celebrityPhotoService;
	private final Random random = new Random();

	@Autowired
	public ResultController(ObjectMapper objectMapper, CelebrityPhotoService celebrityPhotoService) {
		this.objectMapper = objectMapper;
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
			if (jsonObject.has("img")) {
				model.addAttribute("img", jsonObject.get("img").asText());
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

				// 优先使用用户选择的性别进行匹配
				String userGender = jsonObject.has("userGender") ? jsonObject.get("userGender").asText() : null;

				// 获取明星照片URL
				String celebrityPhotoUrl = selectCelebrityPhoto(userGender, gender);
				model.addAttribute("ppei", celebrityPhotoUrl);
			}
			if (jsonObject.has("age")) {
				model.addAttribute("age", jsonObject.get("age").asText());
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
			if (gender != null) {
				// 优先使用用户选择的性别
				String userGender = (String) resultData.get("userGender");

				// 获取明星照片URL
				String celebrityPhotoUrl = selectCelebrityPhoto(userGender, gender);
				response.put("ppei", celebrityPhotoUrl);
			}

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
	 * 根据用户性别选择匹配的明星照片
	 * 修复逻辑：男性匹配男性明星，女性匹配女性明星
	 * 
	 * @param userGender 用户选择的性别（male/female）
	 * @param detectedGender Face++检测的性别（男性/女性）
	 * @return 明星照片URL
	 */
	private String selectCelebrityPhoto(String userGender, String detectedGender) {
		String matchGender;
		
		// 确定匹配的性别（同性匹配）
		if ("male".equals(userGender)) {
			matchGender = "male";
		} else if ("female".equals(userGender)) {
			matchGender = "female";
		} else if ("男性".equals(detectedGender)) {
			matchGender = "male";
		} else {
			matchGender = "female";
		}

		// 获取明星照片URL
		String celebrityPhotoUrl = celebrityPhotoService.getRandomCelebrityPhoto(matchGender);
		
		// 如果获取失败，使用默认本地图片作为降级方案
		if (celebrityPhotoUrl == null || celebrityPhotoUrl.isEmpty()) {
			int randomNum = random.nextInt(10) + 1;
			celebrityPhotoUrl = matchGender + "/" + randomNum + ".png";
			logger.warn("Failed to get celebrity photo, using fallback local image: {}", celebrityPhotoUrl);
		}
		
		return celebrityPhotoUrl;
	}
}

