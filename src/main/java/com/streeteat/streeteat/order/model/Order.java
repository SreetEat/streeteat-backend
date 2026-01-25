package com.streeteat.streeteat.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

import com.streeteat.streeteat.model.User; // existing User entity in your project

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to your existing User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // vendor is not implemented as an entity yet; keep vendorId to reference vendor later
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    // simple representation of ordered items (use JSON or a join table in future)
    @Column(columnDefinition = "text")
    private String items; // e.g. JSON string or comma separated list

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
