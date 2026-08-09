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
     * Get order by id -- restricted to the order's own customer, the
     * order's own vendor, or admin (enforced in OrderService).
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
     * Vendor: list orders for the authenticated vendor's own profile.
     * Previously took a client-supplied vendorId path variable, which meant
     * any VENDOR-role user could list any other vendor's orders. The vendor
     * is now always resolved from the authenticated user's own profile.
     */
    @GetMapping("/vendor/mine")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<OrderResponseDto>> vendorOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Page<OrderResponseDto> p = orderService.getOrdersForVendor(authentication.getName(), PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    /**
     * Vendor accepts an order (must be the order's own vendor, or admin)
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> vendorAccept(@PathVariable Long id, Authentication authentication) {
        OrderResponseDto resp = orderService.vendorAcceptOrder(id, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.ok(resp);
    }

    /**
     * Vendor rejects an order (must be the order's own vendor, or admin)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> vendorReject(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        OrderResponseDto resp = orderService.vendorRejectOrder(id, authentication.getName(), isAdmin(authentication), reason);
        return ResponseEntity.ok(resp);
    }

    /**
     * Vendor marks an order ready -> triggers Haversine-based nearest
     * delivery-partner auto-assignment. This endpoint did not exist at all
     * previously, meaning the assignment logic in DeliveryPartnerService
     * could never actually be triggered through the real API.
     */
    @PostMapping("/{id}/ready")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> markReady(@PathVariable Long id, Authentication authentication) {
        OrderResponseDto resp = orderService.markReady(id, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.ok(resp);
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
