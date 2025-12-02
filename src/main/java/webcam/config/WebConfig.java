package webcam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Web配置类
 * 配置静态资源处理和视图解析
 * 
 * @author Webcam Application
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 静态资源处理 - 支持webapp目录（WAR部署）和标准Spring Boot静态资源目录
		// 注意：在WAR文件中，webapp目录的内容会被复制到WAR根目录
		registry.addResourceHandler("/**")
				.addResourceLocations(
						"classpath:/META-INF/resources/",  // WAR文件中的webapp内容
						"classpath:/static/", 
						"classpath:/public/", 
						"classpath:/resources/",
						"/"  // 根路径，用于WAR部署
				);
		
		// 上传文件访问
		registry.addResourceHandler("/upload/**")
				.addResourceLocations("file:upload/");
	}
	
	/**
	 * JSP视图解析器配置
	 */
	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
}

