package com.streeteat.streeteat.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    @NotNull(message = "vendorId is required")
    private Long vendorId;

    @NotBlank(message = "items cannot be blank")
    private String items; // simple items representation; later replace with list of DTOs

    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;
}
