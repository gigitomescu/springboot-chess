package com.gdt.chess.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration: CORS policy and optional static-resource serving.
 *
 * <p>Changing the active frontend only requires updating {@code frontend.active}
 * in {@code application.yml}; no Java code changes are needed.</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${frontend.active:none}")
    private String activeFrontend;

    @Value("${frontend.cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private String allowedOriginsRaw;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOriginsRaw.split(",");
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        registry.addMapping("/ws/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * Serves the built frontend bundle when {@code frontend.active} is set to
     * {@code angular} or {@code react}.  In development mode each frontend
     * runs on its own dev-server and this handler is not used.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        switch (activeFrontend) {
            case "angular" -> registry
                    .addResourceHandler("/**")
                    .addResourceLocations("file:../frontend/angular-client/dist/chess-angular-client/");
            case "react" -> registry
                    .addResourceHandler("/**")
                    .addResourceLocations("file:../frontend/react-client/dist/");
            default -> { /* no static files */ }
        }
    }
}
