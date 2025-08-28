package com.streeteats.delivery;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



    @RestController
    @RequestMapping("/api/delivery")
    public class DeliveryController {
        @GetMapping("/health")
        public String healthCheck() {
            return "Delivery Service is up!";
        }
    }


