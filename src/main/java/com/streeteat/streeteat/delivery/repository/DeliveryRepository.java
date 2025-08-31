package com.streeteat.streeteat.delivery.repository;

import com.streeteat.streeteat.delivery.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByDeliveryPartnerId(Long partnerId);
}
