package com.streeteat.streeteat.delivery.model;

import com.streeteat.streeteat.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "delivery_partners")
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

    @Column(name = "vehicle_number")
    private String vehicle;

    private String email;
    private String area;

    // Owning user account (role = DELIVERY_PARTNER), same pattern as Vendor.owner.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    // Current status, used to find who is available for a new delivery.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PartnerStatus status = PartnerStatus.AVAILABLE;

    // Last known coordinates, used for Haversine-based nearest-partner assignment.
    private Double latitude;
    private Double longitude;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "deliveryPartner")
    private List<Delivery> deliveries;

    @Builder.Default
    private double earnings = 0.0;
}
