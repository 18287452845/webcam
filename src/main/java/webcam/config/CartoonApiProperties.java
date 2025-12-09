package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云人物动漫化API配置属性类
 * 
 * @author Webcam Application
 */
@Component
@ConfigurationProperties(prefix = "bailian.cartoon.api")
public class CartoonApiProperties {
	
	private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation";
	private String apiKey;
	private String model = "wanx-style-repaint-v1";
	private Integer styleIndex = 3;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getStyleIndex() {
		return styleIndex;
	}

	public void setStyleIndex(Integer styleIndex) {
		this.styleIndex = styleIndex;
	}
}

