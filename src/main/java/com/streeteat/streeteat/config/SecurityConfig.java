package com.streeteat.streeteat.config;

import com.streeteat.streeteat.security.CustomUserDetailsService;
import com.streeteat.streeteat.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // -------- PUBLIC ENDPOINTS --------
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/api/delivery/health",
                                "/error"
                        ).permitAll()

                        // -------- PUBLIC BROWSING (customer discovery) --------
                        // Nearby-vendor search and menu browsing must be readable without login.
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/vendors", "/api/vendors/nearby", "/api/vendors/{id}",
                                "/api/vendors/*/menu", "/api/vendors/*/reviews",
                                "/api/delivery/partners", "/api/delivery/partners/{id}"
                        ).permitAll()

                        // -------- ADMIN --------
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // -------- VENDOR (mutating vendor/menu endpoints) --------
                        .requestMatchers(
                                "/api/vendors/**",
                                "/api/orders/vendor/**"
                        ).hasAnyRole("VENDOR", "ADMIN")

                        // -------- CUSTOMER / ORDER --------
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole("CUSTOMER", "VENDOR", "ADMIN")

                        // -------- DELIVERY PARTNER --------
                        // Role.DELIVERY_PARTNER now matches this matcher (previously the Role
                        // enum only had DELIVERY, so this check always failed for real users).
                        .requestMatchers("/api/delivery/**")
                        .hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                        // -------- EVERYTHING ELSE --------
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
