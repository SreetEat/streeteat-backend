package com.streeteat.streeteat.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String vehicle;
    private String area;

    @OneToMany(mappedBy = "deliveryPartner")
    private List<Delivery> deliveries;

    private double earnings = 0.0;
}
