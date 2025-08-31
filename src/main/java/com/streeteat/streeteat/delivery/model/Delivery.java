package com.streeteat.streeteat.delivery.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private DeliveryPartner deliveryPartner;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
}
