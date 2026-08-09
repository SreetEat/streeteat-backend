package com.streeteat.streeteat.delivery.repository;

import com.streeteat.streeteat.delivery.model.DeliveryPartner;
import com.streeteat.streeteat.delivery.model.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    List<DeliveryPartner> findByStatusAndActiveTrue(PartnerStatus status);
    Optional<DeliveryPartner> findByOwnerId(Long ownerId);
    Optional<DeliveryPartner> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<DeliveryPartner> findByActiveTrue(Pageable pageable);
}
