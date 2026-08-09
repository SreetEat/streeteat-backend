package com.streeteat.streeteat.delivery.dto;

import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDto {
    private Long id;
    private Long orderId;
    private Long partnerId;
    private DeliveryStatus status;
}
