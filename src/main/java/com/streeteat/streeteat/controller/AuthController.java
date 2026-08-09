package com.streeteat.streeteat.controller;

import com.streeteat.streeteat.auth.dto.AuthResponseDto;
import com.streeteat.streeteat.auth.dto.LoginRequestDto;
import com.streeteat.streeteat.auth.dto.RegisterRequestDto;
import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.model.Role;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("An account with this email already exists");
        }

        // Prevent clients from self-assigning ADMIN via registration.
        Role role = request.getRole() == null || request.getRole() == Role.ADMIN
                ? Role.CUSTOMER
                : request.getRole();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());

        return ResponseEntity.ok(toAuthResponse(saved, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String token = jwtUtil.generateToken(authentication.getName());
        return ResponseEntity.ok(toAuthResponse(user, token));
    }

    private AuthResponseDto toAuthResponse(User user, String token) {
        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
