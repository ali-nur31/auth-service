package com.example.auth.service;

import com.example.auth.dto.GenericResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.DuplicateFieldException;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtProvider;
import com.example.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void loginSuccess() {
        User user = user();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtProvider.generateAccessToken(user)).thenReturn("access.jwt");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh-uuid");

        LoginRequest dto = new LoginRequest();
        dto.setUsernameOrEmail("test");
        dto.setPassword("password123");
        LoginResponse loginResponse = authService.login(dto);

        assertThat(loginResponse.getAccessToken()).isEqualTo("access.jwt");
        assertThat(loginResponse.getRefreshToken()).isEqualTo("refresh-uuid");
    }

    @Test
    void loginFailsWithWrongPassword() {
        User u = user();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", u.getPasswordHash())).thenReturn(false);

        LoginRequest dto = new LoginRequest();
        dto.setUsernameOrEmail("test");
        dto.setPassword("bad");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void registerCreatesUser() {
        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(newRole("ROLE_USER")));

        RegisterRequest dto = new RegisterRequest();
        dto.setName("Test");
        dto.setUsername("test");
        dto.setEmail("test@gmail.com");
        dto.setPhoneNumber("777123123");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");

        GenericResponse resp = authService.register(dto);

        assertThat(resp.getMessage()).isEqualTo("User registered successfully!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerFailsOnDuplicateEmail() {
        when(userRepository.existsByEmail("dup@mail.com")).thenReturn(true);

        RegisterRequest dto = new RegisterRequest();
        dto.setName("Dup");
        dto.setUsername("dup");
        dto.setEmail("dup@mail.com");
        dto.setPhoneNumber("777");
        dto.setPassword("pwd123456");
        dto.setConfirmPassword("pwd123456");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(DuplicateFieldException.class);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@gmail.com");
        user.setPasswordHash("cGFzc3dvcmQxMjM=");
        user.setRoles(Set.of(newRole("ROLE_USER")));

        return user;
    }

    private Role newRole(String name) {
        Role role = new Role();
        role.setId(1L);
        role.setName(name);

        return role;
    }
}
