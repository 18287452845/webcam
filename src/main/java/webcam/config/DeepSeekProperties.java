package webcam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API配置属性
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Component
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekProperties {

    private String url;
    private String key;
    private String model = "deepseek-chat";
    private double temperature = 0.7;
    private int maxTokens = 500;

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}
