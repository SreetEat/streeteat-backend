package com.streeteat.streeteat.order.controller;

import com.streeteat.streeteat.order.dto.*;
import com.streeteat.streeteat.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    /**
     * Place an order
     * Allowed roles: CUSTOMER (and ADMIN optionally)
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> placeOrder(
            @Valid @RequestBody OrderRequestDto req,
            Authentication authentication
    ) {
        String username = authentication.getName();
        OrderResponseDto resp = orderService.placeOrder(req, username);
        return ResponseEntity.ok(resp);
    }

    /**
     * Get order by id (customer, vendor or admin)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        OrderResponseDto resp = orderService.getOrder(id, username);
        return ResponseEntity.ok(resp);
    }

    /**
     * List orders for the logged-in customer
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Page<OrderResponseDto> p = orderService.getOrdersForCustomer(username, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    /**
     * Vendor: list orders for vendor
     */
    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> vendorOrders(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OrderResponseDto> p = orderService.getOrdersForVendor(vendorId, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    /**
     * Vendor accepts an order
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<OrderResponseDto> vendorAccept(@PathVariable Long id, @RequestParam Long vendorId) {
        OrderResponseDto resp = orderService.vendorAcceptOrder(id, vendorId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Vendor rejects an order
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<OrderResponseDto> vendorReject(@PathVariable Long id, @RequestParam Long vendorId, @RequestParam(required = false) String reason) {
        OrderResponseDto resp = orderService.vendorRejectOrder(id, vendorId, reason);
        return ResponseEntity.ok(resp);
    }
}
