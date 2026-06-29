package com.example.tmdt_bookingmakeup_app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class JwtHttpInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // cut "Bearer" from string

            // Validate Token
            if (jwtConfig.isValid(token)) {
                UUID userId = jwtConfig.extractUserId(token);
                request.setAttribute("userId", userId.toString());
                return true;
            }
        }

        // The promotion listing is public. If a valid token is supplied above,
        // the controller still receives the user identity and can apply admin/
        // service-owner rules. Without a token it returns active promotions.
        if (HttpMethod.GET.matches(request.getMethod())
                && (request.getRequestURI().startsWith("/services")
                    || "/promotions".equals(request.getRequestURI()))) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized: Token is missing or invalid");
        return false;
    }
}
