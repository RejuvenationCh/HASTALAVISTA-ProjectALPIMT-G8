package dev.outfix.comfyui;

import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ComfyUiProperties.class)
public class ComfyUiConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebClient comfyUiWebClient(ComfyUiProperties props) {
        return WebClient.builder()
            .baseUrl(props.getBaseUrl())
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
