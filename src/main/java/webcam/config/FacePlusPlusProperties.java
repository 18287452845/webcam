package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Face++ API配置属性类
 * 
 * @author Webcam Application
 */
@Component
@ConfigurationProperties(prefix = "faceplusplus.api")
public class FacePlusPlusProperties {
	
	private String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
	private String key;
	private String secret;
	private String returnAttributes = "gender,age,smiling,eyestatus,glass,headpose,facequality,blur";
	private String returnLandmark = "0";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getReturnAttributes() {
		return returnAttributes;
	}

	public void setReturnAttributes(String returnAttributes) {
		this.returnAttributes = returnAttributes;
	}

	public String getReturnLandmark() {
		return returnLandmark;
	}

	public void setReturnLandmark(String returnLandmark) {
		this.returnLandmark = returnLandmark;
	}
}

