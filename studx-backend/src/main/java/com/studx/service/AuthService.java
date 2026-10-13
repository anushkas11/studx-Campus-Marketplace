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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final List<String> approvedEmailDomains;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       @Value("${app.email.approved-domains:.edu,.ac.in,.edu.in}") String approvedDomains) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.approvedEmailDomains = Arrays.stream(approvedDomains.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
    }

    @Transactional
    public AuthResponse register(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User already exists with email: " + email + ", you can login");
        }

        boolean isCollegeEmail = isApprovedCollegeDomain(email);

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCollege(request.getCollege());
        user.setCity(request.getCity());
        user.setVerified(isCollegeEmail);
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return new AuthResponse(true, "Signup successful");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Authentication failed: Email or Password is wrong"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Authentication failed: Email or Password is wrong");
        }

        String token = tokenProvider.generateTokenForUser(user.getId(), user.getEmail(), user.getName());

        return new AuthResponse(true, "Login successful", token, user.getName(), user.getEmail());
    }

    public boolean isApprovedCollegeDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        for (String approved : approvedEmailDomains) {
            if (domain.endsWith(approved.startsWith(".") ? approved.substring(1) : approved)) {
                return true;
            }
        }
        return false;
    }
}
