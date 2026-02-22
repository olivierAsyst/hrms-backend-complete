package com.company.hrms.service;

import com.company.hrms.dto.auth.AuthResponse;
import com.company.hrms.dto.auth.LoginRequest;
import com.company.hrms.dto.auth.RegisterRequest;
import com.company.hrms.entity.RefreshToken;
import com.company.hrms.entity.Role;
import com.company.hrms.entity.User;
import com.company.hrms.enums.RoleType;
import com.company.hrms.exception.BadRequestException;
import com.company.hrms.exception.DuplicateResourceException;
import com.company.hrms.repository.RefreshTokenRepository;
import com.company.hrms.repository.RoleRepository;
import com.company.hrms.repository.UserRepository;
import com.company.hrms.security.JwtTokenProvider;
import com.company.hrms.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse refreshToken(String requestToken) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(requestToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getAuthorities()
                );

        String newAccessToken = tokenProvider.generateToken(authentication);

        // Log audit
        auditService.logAuthentication("REFRESH_TOKEN", user.getUsername(), true, null);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet()))
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);

            // Get user details
            User user = userRepository.findByUsernameAndDeletedFalse(loginRequest.getUsername())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            // Update last login
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

            // Log audit
            auditService.logAuthentication("LOGIN", user.getUsername(), true, null);

            // Build response
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet());

            Set<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName().name())
                    .collect(Collectors.toSet());

            return AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken.getToken())
                    .type("Bearer")
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

        } catch (Exception ex) {
            // Log failed login attempt
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user != null) {
                userRepository.incrementFailedLoginAttempts(user.getId());
                
                // Lock account after 5 failed attempts
                User updatedUser = userRepository.findById(user.getId()).orElse(null);
                if (updatedUser != null && updatedUser.getFailedLoginAttempts() >= 5) {
                    LocalDateTime lockedUntil = LocalDateTime.now().plusHours(1);
                    userRepository.lockAccount(user.getId(), false, lockedUntil);
                    log.warn("Account locked for user: {} until {}", user.getUsername(), lockedUntil);
                }
                
                auditService.logAuthentication("LOGIN", loginRequest.getUsername(), false, ex.getMessage());
            }
            
            throw ex;
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("User", "username", registerRequest.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", registerRequest.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .failedLoginAttempts(0)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()) {
            for (String roleName : registerRequest.getRoles()) {
                try {
                    RoleType roleType = RoleType.valueOf(roleName);
                    Role role = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid role: " + roleName);
                }
            }
        } else {
            // Default role: EMPLOYEE
            Role userRole = roleRepository.findByName(RoleType.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new BadRequestException("Default role not found"));
            roles.add(userRole);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Log audit
        auditService.logUserRegistration(savedUser.getUsername());

        // Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        Set<String> permissions = savedUser.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName().name())
                .collect(Collectors.toSet());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .roles(roleNames)
                .permissions(permissions)
                .build();
    }

    public void logout(String refreshToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            auditService.logAuthentication("LOGOUT", username, true, null);
            refreshTokenService.revokeToken(refreshToken);
            SecurityContextHolder.clearContext();
        }
    }
}
