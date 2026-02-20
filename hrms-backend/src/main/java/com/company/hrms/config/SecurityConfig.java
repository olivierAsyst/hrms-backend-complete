package com.company.hrms.config;

import com.company.hrms.security.CustomUserDetailsService;
import com.company.hrms.security.JwtAuthenticationEntryPoint;
import com.company.hrms.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/*/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // Employee endpoints
                        .requestMatchers(HttpMethod.GET, "/api/*/employees/**").hasAnyAuthority("EMPLOYEE_READ", "ROLE_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/*/employees/**").hasAnyAuthority("EMPLOYEE_CREATE", "ROLE_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/*/employees/**").hasAnyAuthority("EMPLOYEE_UPDATE", "ROLE_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/*/employees/**").hasAnyAuthority("EMPLOYEE_DELETE", "ROLE_ADMIN", "ROLE_HR_MANAGER")
                        
                        // Department endpoints
                        .requestMatchers(HttpMethod.GET, "/api/*/departments/**").hasAnyAuthority("DEPARTMENT_READ", "ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/*/departments/**").hasAnyAuthority("DEPARTMENT_CREATE", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/*/departments/**").hasAnyAuthority("DEPARTMENT_UPDATE", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/*/departments/**").hasAnyAuthority("DEPARTMENT_DELETE", "ROLE_ADMIN")
                        
                        // User management
                        .requestMatchers("/api/*/users/**").hasAnyAuthority("USER_READ", "ROLE_ADMIN")
                        
                        // Role management
                        .requestMatchers("/api/*/roles/**").hasAuthority("ROLE_SUPER_ADMIN")
                        
                        // Audit logs
                        .requestMatchers("/api/*/audit/**").hasAnyAuthority("AUDIT_READ", "ROLE_ADMIN")
                        
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
