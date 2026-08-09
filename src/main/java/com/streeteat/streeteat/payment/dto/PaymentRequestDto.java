package com.streeteat.streeteat.payment.dto;

import com.streeteat.streeteat.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotNull(message = "method is required")
    private PaymentMethod method;
}
