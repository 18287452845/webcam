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
	private final Random random = new Random();

	@Autowired
	public ResultController(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
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

				// 根据用户选择的性别选择匹配图片
				int randomNum = random.nextInt(10) + 1;
				if ("male".equals(userGender)) {
					// 用户选择了男性，匹配女性照片
					model.addAttribute("ppei", "female/" + randomNum + ".png");
				} else if ("female".equals(userGender)) {
					// 用户选择了女性，匹配男性照片
					model.addAttribute("ppei", "male/" + randomNum + ".png");
				} else {
					// 如果没有用户选择性别，使用Face++检测结果
					if ("男性".equals(gender)) {
						model.addAttribute("ppei", "female/" + randomNum + ".png");
					} else {
						model.addAttribute("ppei", "male/" + randomNum + ".png");
					}
				}
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

				int randomNum = random.nextInt(10) + 1;
				if ("male".equals(userGender)) {
					// 用户选择了男性，匹配女性照片
					response.put("ppei", "female/" + randomNum + ".png");
				} else if ("female".equals(userGender)) {
					// 用户选择了女性，匹配男性照片
					response.put("ppei", "male/" + randomNum + ".png");
				} else {
					// 如果没有用户选择性别，使用Face++检测结果
					if ("男性".equals(gender)) {
						response.put("ppei", "female/" + randomNum + ".png");
					} else {
						response.put("ppei", "male/" + randomNum + ".png");
					}
				}
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
}

