package com.streeteat.streeteat.controller;

import com.streeteat.streeteat.model.User;

import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully!";
    }


    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),   // using email as username
                        user.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {
            String token = jwtUtil.generateToken(authentication.getName()); // generate token with email
            return "Bearer " + token;  // return token to client
        } else {
            return "Invalid credentials!";
        }
    }
}
