package com.streeteat.streeteat.delivery.dto;

import com.streeteat.streeteat.delivery.model.PartnerStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String vehicle;
    private String area;
    private PartnerStatus status;
    private Double latitude;
    private Double longitude;
    private boolean active;
    private double earnings;
}
