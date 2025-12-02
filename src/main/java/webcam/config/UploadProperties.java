package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性类
 * 
 * @author Webcam Application
 */
@Component
@ConfigurationProperties(prefix = "webcam.upload")
public class UploadProperties {
	
	private String path = "upload/";
	private String baseUrl = "http://localhost:8080/upload/";

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}

