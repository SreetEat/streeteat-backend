package com.streeteat.streeteat.review.repository;

import com.streeteat.streeteat.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByVendorId(Long vendorId, Pageable pageable);
    Optional<Review> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
