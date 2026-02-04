package com.streeteat.streeteat.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Info Contributor for /actuator/info endpoint.
 * Provides application metadata like name, version, description,
 * build timestamp, and environment.
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appInfo = new HashMap<>();

        // Basic application info
        appInfo.put("name", "Streeteat Backend");
        appInfo.put("version", "1.0.0");
        appInfo.put("description", "Street food ordering backend API");

        // Optional additional info
        appInfo.put("buildTime", LocalDateTime.now().toString());
        appInfo.put("environment", System.getProperty("spring.profiles.active", "dev"));

        builder.withDetail("app", appInfo);
    }
}
