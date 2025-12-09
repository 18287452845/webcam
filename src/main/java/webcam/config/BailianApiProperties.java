package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云百炼API配置属性类
 * 
 * @author Webcam Application
 */
@Component
@ConfigurationProperties(prefix = "bailian.api")
public class BailianApiProperties {
	
	private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
	private String apiKey;
	private String model = "qwen-vl-max";
	private Double temperature = 0.7;
	private Integer maxTokens = 2000;
	private Integer maxHealthAnalysisLength = 600;

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

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Integer getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public Integer getMaxHealthAnalysisLength() {
		return maxHealthAnalysisLength;
	}

	public void setMaxHealthAnalysisLength(Integer maxHealthAnalysisLength) {
		this.maxHealthAnalysisLength = maxHealthAnalysisLength;
	}
}

