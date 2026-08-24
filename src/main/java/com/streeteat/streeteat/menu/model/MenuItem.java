package com.streeteat.streeteat.menu.model;

import com.streeteat.streeteat.vendor.model.Vendor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false, length = 120)
    private String name;

    // TEXT, not VARCHAR: may hold a base64 data URI (no object storage
    // wired up yet -- see V12__menu_item_image.sql for the honest caveat
    // on this being an MVP choice, not the long-term design).
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
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