package com.ashish.rentflatmatefinder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {
    private String apiKey;
    private String apiUrl;
    private String prompt;
    private String model;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
