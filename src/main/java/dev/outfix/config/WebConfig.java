package dev.outfix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Makes uploaded files (images) accessible via HTTP.
 *
 * Files saved to the local "uploads/" folder on the server
 * can be accessed through the "/uploads/**" URL path.
 * Example: a file at uploads/face-models/1/photo.jpg
 * is reachable at http://localhost:8080/uploads/face-models/1/photo.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDirectory + "/");
    }
}
