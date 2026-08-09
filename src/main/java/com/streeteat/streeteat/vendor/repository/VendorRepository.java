package com.streeteat.streeteat.vendor.repository;

import com.streeteat.streeteat.vendor.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByEmail(String email);
    Optional<Vendor> findByOwnerId(Long ownerId);
    List<Vendor> findByActiveTrue();
    boolean existsByEmail(String email);
}
