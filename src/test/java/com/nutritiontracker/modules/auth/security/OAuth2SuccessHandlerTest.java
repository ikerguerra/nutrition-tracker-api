package com.nutritiontracker.modules.auth.security;

import com.nutritiontracker.modules.auth.dto.AuthResponse;
import com.nutritiontracker.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    @Test
    void onAuthenticationSuccess_shouldRedirectWithTokens() throws Exception {
        // Arrange
        String registrationId = "google";
        String accessToken = "testAccessToken";
        String refreshToken = "testRefreshToken";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        when(authService.loginOAuth2(oAuth2User, registrationId)).thenReturn(authResponse);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedUrl = "http://localhost:5173/auth/callback?token=" + accessToken + "&refresh=" + refreshToken;
        verify(response).sendRedirect(expectedUrl);
    }
}
