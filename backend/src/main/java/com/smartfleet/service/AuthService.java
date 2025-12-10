package com.smartfleet.service;

import com.smartfleet.dto.LoginRequestDTO;
import com.smartfleet.dto.RegisterRequestDTO;
import com.smartfleet.dto.AuthResponseDTO;
import com.smartfleet.entity.User;
import com.smartfleet.entity.Driver;
import com.smartfleet.repository.UserRepository;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.security.JwtService; // ← SCHIMBAT de la JwtTokenProvider
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service for user login and registration
 * CRITICAL: Integrates with EmailService for notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final JwtService jwtService; // ← SCHIMBAT de la JwtTokenProvider
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Handle user login
     * If driver, send login notification email
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // CRITICAL: Send login notification for drivers
        if ("ROLE_DRIVER".equals(user.getRole())) {
            try {
                emailService.sendDriverLoginNotification(user.getUsername(), user.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send login notification email: {}", e.getMessage());
            }
        }

        // Generate token with role and email
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Handle user registration
     * Special case: username "admin" becomes ROLE_ADMIN
     * Others become ROLE_DRIVER with driver profile
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if user exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus("ACTIVE");

        // SPECIAL CASE: If username is "admin", create as ADMIN role
        if ("admin".equals(request.getUsername())) {
            user.setRole("ROLE_ADMIN");
            user = userRepository.save(user);
            log.info("Admin registered: {}", request.getUsername());
        } else {
            // Normal case: Create as DRIVER with driver profile
            user.setRole("ROLE_DRIVER");
            user = userRepository.save(user);

            // Create driver profile
            // CRITICAL: vehicle_id is NULL initially - driver must select vehicle
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setName(request.getFullName());
            driver.setLicense(request.getLicense());
            driver.setStatus("AVAILABLE");
            driver.setVehicle(null);
            driverRepository.save(driver);

            log.info("Driver registered: {} ({}) - No vehicle assigned", request.getFullName(), request.getUsername());

            // CRITICAL: Send welcome email asynchronously (with error handling)
            try {
                emailService.sendWelcomeEmail(user.getEmail(), request.getFullName());
            } catch (Exception e) {
                log.warn("Failed to send welcome email: {}", e.getMessage());
            }
        }

        // Generate token with role and email
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}