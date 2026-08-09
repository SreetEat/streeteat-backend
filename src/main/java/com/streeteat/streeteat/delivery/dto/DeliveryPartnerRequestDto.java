package com.streeteat.streeteat.delivery.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerRequestDto {

    @NotBlank(message = "name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @Pattern(regexp = "^[0-9]{7,15}$", message = "phone must be 7-15 digits")
    private String phone;

    private String vehicle;
    private String area;

    private Double latitude;
    private Double longitude;
}
