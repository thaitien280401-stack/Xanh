package com.xanh.vocabulary.service;

import com.xanh.vocabulary.config.AppProperties;
import com.xanh.vocabulary.dto.request.LoginRequest;
import com.xanh.vocabulary.dto.request.RegisterRequest;
import com.xanh.vocabulary.dto.response.AuthResponse;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.repository.RefreshTokenRepository;
import com.xanh.vocabulary.repository.UserRepository;
import com.xanh.vocabulary.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock AppProperties appProperties;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        AppProperties.Jwt jwtProps = new AppProperties.Jwt();
        jwtProps.setRefreshExpirationMs(604800000L);
        when(appProperties.getJwt()).thenReturn(jwtProps);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("user", "test@example.com", "password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void register_shouldThrow_whenUsernameAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("takenuser", "new@example.com", "password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void register_shouldSucceed_withValidData() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hashed_password")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("new@example.com").password("hashed").roles("USER").build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(new RegisterRequest("newuser", "new@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.email()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }
}
