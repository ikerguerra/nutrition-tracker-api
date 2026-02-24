package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.dto.AuthResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.enums.AuthProvider;
import com.nutritiontracker.modules.auth.repository.UserRepository;
import com.nutritiontracker.modules.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginOAuth2_shouldCreateNewUser_whenUserDoesNotExist() {
        // Arrange
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String providerId = "google";

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("given_name")).thenReturn(firstName);
        when(oAuth2User.getAttribute("family_name")).thenReturn(lastName);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(tokenProvider.generateAccessToken(email, firstName, lastName)).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(email)).thenReturn("refreshToken");

        // Act
        AuthResponse response = authService.loginOAuth2(oAuth2User, providerId);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userRepository).save(any(User.class));
        verify(userProfileService).createDefaultProfile(savedUser);
    }

    @Test
    void loginOAuth2_shouldReturnTokens_whenUserExists() {
        // Arrange
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String providerId = "google";

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("given_name")).thenReturn(firstName);
        when(oAuth2User.getAttribute("family_name")).thenReturn(lastName);

        User existingUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        when(tokenProvider.generateAccessToken(email, firstName, lastName)).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(email)).thenReturn("refreshToken");

        // Act
        AuthResponse response = authService.loginOAuth2(oAuth2User, providerId);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userRepository, never()).save(any(User.class));
        verify(userProfileService, never()).createDefaultProfile(any(User.class));
    }
}
