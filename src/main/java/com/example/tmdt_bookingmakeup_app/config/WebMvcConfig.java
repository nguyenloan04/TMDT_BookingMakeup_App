package com.example.tmdt_bookingmakeup_app.config;

import com.example.tmdt_bookingmakeup_app.security.JwtHttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtHttpInterceptor jwtHttpInterceptor;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Protect endpoints such as profile updates, listings, bookings, etc.
        // Exclude authentication and registration pathways, public searches
        registry.addInterceptor(jwtHttpInterceptor)
                .addPathPatterns("/users/**", "/chats/**", "/messages/**", "/promotions/**", "/bookings/**", "/artists/**", "/services/**", "/favourites/**", "/reviews/**", "/payment/**")
                .excludePathPatterns("/auth/**", "/verification/**", "/search/**", "/users/{id:[a-fA-F0-9-]+}", "/promotions/{id:[a-fA-F0-9-]+}", "/promotions/validate", "/bookings/artist/**", "/profile/providers/**", "/services/{id:[a-fA-F0-9-]+}", "/reviews/service/{serviceId:[a-fA-F0-9-]+}", "/reviews/artist/{artistId:[a-fA-F0-9-]+}");
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(",")) // Dùng split(",") để lỡ sau này bạn điền nhiều link
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
