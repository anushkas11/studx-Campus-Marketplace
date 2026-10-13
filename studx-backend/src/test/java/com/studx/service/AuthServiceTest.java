package com.studx.service;

import com.studx.dto.AuthResponse;
import com.studx.dto.LoginRequest;
import com.studx.dto.SignupRequest;
import com.studx.entity.Role;
import com.studx.entity.User;
import com.studx.exception.BadRequestException;
import com.studx.exception.ConflictException;
import com.studx.repository.UserRepository;
import com.studx.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider, ".edu,.ac.in,.edu.in");
    }

    @Test
    void testRegisterWithApprovedCollegeEmail_shouldBeVerified() {
        SignupRequest request = new SignupRequest("Rahul Kumar", "rahul@iitd.ac.in", "Password@123", "IIT Delhi", "Delhi");

        when(userRepository.existsByEmail("rahul@iitd.ac.in")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        AuthResponse response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Signup successful", response.getMessage());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("rahul@iitd.ac.in") &&
                user.isVerified() &&
                user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    void testRegisterWithGeneralEmail_shouldNotBeVerifiedByDefault() {
        SignupRequest request = new SignupRequest("Amit Shah", "amit@gmail.com", "Password@123", "DU", "Delhi");

        when(userRepository.existsByEmail("amit@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        AuthResponse response = authService.register(request);

        assertTrue(response.isSuccess());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("amit@gmail.com") &&
                !user.isVerified()
        ));
    }

    @Test
    void testRegisterDuplicateEmail_shouldThrowConflictException() {
        SignupRequest request = new SignupRequest("Rahul", "rahul@iitd.ac.in", "Password@123", "IIT", "Delhi");
        when(userRepository.existsByEmail("rahul@iitd.ac.in")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginValidCredentials_shouldReturnJwtToken() {
        LoginRequest request = new LoginRequest("rahul@iitd.ac.in", "Password@123");

        User user = new User("Rahul", "rahul@iitd.ac.in", "encodedPass", "IIT", "Delhi", Role.ROLE_USER);
        user.setId(10L);

        when(userRepository.findByEmail("rahul@iitd.ac.in")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPass")).thenReturn(true);
        when(tokenProvider.generateTokenForUser(10L, "rahul@iitd.ac.in", "Rahul")).thenReturn("dummy.jwt.token");

        AuthResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("dummy.jwt.token", response.getJwtToken());
        assertEquals("Rahul", response.getName());
    }

    @Test
    void testLoginInvalidPassword_shouldThrowBadRequestException() {
        LoginRequest request = new LoginRequest("rahul@iitd.ac.in", "WrongPassword");

        User user = new User("Rahul", "rahul@iitd.ac.in", "encodedPass", "IIT", "Delhi", Role.ROLE_USER);

        when(userRepository.findByEmail("rahul@iitd.ac.in")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPass")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
