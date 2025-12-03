package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.dto.AuthResponse;
import com.nutritiontracker.modules.auth.dto.LoginRequest;
import com.nutritiontracker.modules.auth.dto.RegisterRequest;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.enums.AuthProvider;
import com.nutritiontracker.modules.auth.repository.UserRepository;
import com.nutritiontracker.modules.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserProfileService userProfileService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        userProfileService.createDefaultProfile(savedUser);

        String accessToken = tokenProvider.generateAccessToken(savedUser.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        String accessToken = tokenProvider.generateAccessToken(request.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = tokenProvider.generateAccessToken(email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
