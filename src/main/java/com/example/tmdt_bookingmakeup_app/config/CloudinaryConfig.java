package com.example.tmdt_bookingmakeup_app.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary getCloudinary(CloudinaryProperties props) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", props.getName(),
                "api_key", props.getApiKey(),
                "api_secret", props.getSecret())
        );
    }

    @Component
    @Getter
    @Setter
    @ConfigurationProperties(prefix = "cloudinary")
    public static class CloudinaryProperties {
        private String name;
        private String apiKey;
        private String secret;
    }
}
