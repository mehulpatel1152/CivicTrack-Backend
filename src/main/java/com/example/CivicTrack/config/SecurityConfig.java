package com.example.CivicTrack.config;

import com.example.CivicTrack.security.JwtFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasAuthority("ROLE_ADMIN")

                        .requestMatchers("/api/complaints/*/verify").hasAuthority("ROLE_VERIFIER")
                        .requestMatchers("/api/complaints/*/reject").hasAuthority("ROLE_VERIFIER")

                        .requestMatchers("/api/complaints/*/assign/**").hasAuthority("ROLE_AUTHORITY")
                        .requestMatchers("/api/complaints/*/approve").hasAuthority("ROLE_AUTHORITY")
                        .requestMatchers("/api/complaints/*/reject-after-done").hasAuthority("ROLE_AUTHORITY")

                        .requestMatchers("/api/complaints/*/start").hasAuthority("ROLE_DEPARTMENT")
                        .requestMatchers("/api/complaints/*/done").hasAuthority("ROLE_DEPARTMENT")
//                      .requestMatchers("/api/complaints/*/complete").hasAuthority("ROLE_DEPARTMENT")

                        .requestMatchers("/api/complaints")
                        .hasAnyAuthority("ROLE_USER", "ROLE_AUTHORITY", "ROLE_ADMIN")
                        .requestMatchers("/api/complaints/**").authenticated()

                        .requestMatchers("/api/complaints/dashboard")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_AUTHORITY")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}