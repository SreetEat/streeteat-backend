package com.streeteat.streeteat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // Protected route - only accessible with valid JWT
    @GetMapping("/users/me")
    public String getMyProfile() {
        return "You have accessed a protected endpoint!";
    }
}

