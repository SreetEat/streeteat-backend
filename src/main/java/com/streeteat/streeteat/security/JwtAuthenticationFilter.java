package com.streeteat.streeteat.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        // BUG THAT WAS HERE: extractUsername()/validateToken() were called
        // with no try/catch. Any invalid token -- expired, tampered,
        // malformed, wrong signature -- throws a raw io.jsonwebtoken
        // exception straight out of this filter, which Spring has no
        // handler for, so it becomes an unhandled 500 instead of the
        // 401 Unauthorized a bad/expired token should produce. Confirmed
        // live: a week-old expired token in a browser's localStorage
        // caused exactly this on both the customer and vendor apps.
        //
        // Fix: catch JwtException (parent of ExpiredJwtException,
        // MalformedJwtException, SignatureException, UnsupportedJwtException)
        // and IllegalArgumentException (null/empty token), log it at debug
        // level, and simply don't set authentication for this request.
        // Spring Security's own entry point then correctly returns 401/403
        // for any endpoint that requires auth, exactly as if no token had
        // been sent at all -- no more raw 500s from bad tokens.
        try {
            String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejected invalid/expired JWT on {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            // Deliberately no authentication set -- request proceeds
            // unauthenticated, and Spring Security handles the 401/403
            // for any endpoint that actually requires it.
        }

        filterChain.doFilter(request, response);
    }
}