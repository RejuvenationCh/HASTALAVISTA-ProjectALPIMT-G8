package dev.outfix.comfyui;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Wires up the ComfyUI integration beans.
 *
 * Creates a WebClient pointed at the ComfyUI server (default: localhost:8188).
 * The base URL is configurable via comfyui.base-url in application.properties.
 */
@Configuration
@EnableConfigurationProperties(ComfyUiProperties.class)
public class ComfyUiConfig {

    /**
     * HTTP client used to talk to the ComfyUI API.
     * All requests from ComfyUiService go through this client.
     */
    @Bean
    public WebClient comfyUiWebClient(ComfyUiProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
