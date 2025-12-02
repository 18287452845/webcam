package webcam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot主启动类
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WebcamApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebcamApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(WebcamApplication.class, args);
	}
}

