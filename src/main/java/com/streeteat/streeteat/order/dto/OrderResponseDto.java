package com.streeteat.streeteat.order.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import com.streeteat.streeteat.order.model.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long customerId;
    private Long vendorId;
    private String items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
