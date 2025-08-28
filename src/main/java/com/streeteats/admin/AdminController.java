package com.streeteats.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Admin Module is up!";
    }
}