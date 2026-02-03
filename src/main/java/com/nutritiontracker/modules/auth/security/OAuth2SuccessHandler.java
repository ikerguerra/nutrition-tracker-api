package com.nutritiontracker.modules.auth.security;

import com.nutritiontracker.modules.auth.dto.AuthResponse;
import com.nutritiontracker.modules.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public OAuth2SuccessHandler(@org.springframework.context.annotation.Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

        // Generate JWT tokens
        AuthResponse authResponse = authService.loginOAuth2(oAuth2User, registrationId);

        // Redirect to Frontend with tokens
        String targetUrl = "http://localhost:5173/auth/callback" +
                "?token=" + authResponse.getAccessToken() +
                "&refresh=" + authResponse.getRefreshToken();

        response.sendRedirect(targetUrl);
    }
}
