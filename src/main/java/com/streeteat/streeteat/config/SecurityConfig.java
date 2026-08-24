package com.streeteat.streeteat.config;

import com.streeteat.streeteat.security.CustomUserDetailsService;
import com.streeteat.streeteat.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:3000}")
    private List<String> allowedOrigins;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BUG THAT WAS HERE: no formLogin()/httpBasic() means Spring Security
    // never registers a real AuthenticationEntryPoint. Its fallback in that
    // case is Http403ForbiddenEntryPoint, so EVERY "not authenticated at
    // all" case (missing token, expired token, malformed token) came back
    // as 403 Forbidden instead of 401 Unauthorized. That's a meaningful
    // difference for API clients: a 401 means "log in again", a 403 means
    // "you're logged in but not allowed to do this" -- collapsing both into
    // 403 broke the frontend's ability to tell an expired session apart
    // from a genuine permissions error. Confirmed live: a stale JWT on
    // POST /api/vendors returned 403, not 401, even though the actual
    // problem was purely "this token isn't valid," nothing to do with role.
    //
    // Fix: register an explicit AuthenticationEntryPoint that sends 401 for
    // the "not authenticated" case. Role-based denial for an authenticated
    // user who lacks the right role still correctly falls through to
    // Spring's default AccessDeniedHandler (403) -- that distinction is
    // preserved, not collapsed.
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication required");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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
                        .requestMatchers("/api/delivery/**")
                        .hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                        // -------- EVERYTHING ELSE --------
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))

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