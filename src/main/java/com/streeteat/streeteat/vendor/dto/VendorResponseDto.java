package com.streeteat.streeteat.vendor.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double distanceKm; // populated only for "nearby" search results
    private boolean active;
    private Instant createdAt;
}
