package com.example.tmdt_bookingmakeup_app.config;

import com.example.tmdt_bookingmakeup_app.security.JwtHttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtHttpInterceptor jwtHttpInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Protect endpoints such as profile updates, listings, bookings, etc.
        // Exclude authentication and registration pathways, public searches
        registry.addInterceptor(jwtHttpInterceptor)
                .addPathPatterns("/users/**", "/chats/**", "/messages/**", "/promotions/**", "/bookings/**")
                .excludePathPatterns("/auth/**", "/verification/**", "/search/**", "/users/{id}", "/promotions", "/promotions/{id}", "/promotions/validate");
    }
}
