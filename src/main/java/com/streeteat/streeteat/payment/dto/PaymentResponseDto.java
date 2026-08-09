package com.streeteat.streeteat.payment.dto;

import com.streeteat.streeteat.payment.model.PaymentMethod;
import com.streeteat.streeteat.payment.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private Instant createdAt;
}
