package com.streeteat.streeteat.order.repository;

import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomer(User customer, Pageable pageable);
    Page<Order> findByVendorId(Long vendorId, Pageable pageable);
    List<Order> findByVendorId(Long vendorId);
}
