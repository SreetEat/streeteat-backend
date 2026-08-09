package com.streeteat.streeteat.menu.repository;

import com.streeteat.streeteat.menu.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByVendorId(Long vendorId);
    List<MenuItem> findByVendorIdAndAvailableTrue(Long vendorId);
}
