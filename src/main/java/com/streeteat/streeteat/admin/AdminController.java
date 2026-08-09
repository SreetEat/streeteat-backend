package com.streeteat.streeteat.admin;

import com.streeteat.streeteat.admin.dto.AdminUserDto;
import com.streeteat.streeteat.delivery.dto.DeliveryPartnerResponseDto;
import com.streeteat.streeteat.order.dto.OrderResponseDto;
import com.streeteat.streeteat.payment.dto.PaymentResponseDto;
import com.streeteat.streeteat.review.dto.ReviewResponseDto;
import com.streeteat.streeteat.vendor.dto.VendorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin APIs", description = "Admin-only listing and delete operations across the platform")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/health")
    public String healthCheck() {
        return "Admin Module is up!";
    }

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listUsers(PageRequest.of(page, size)));
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all vendors")
    @GetMapping("/vendors")
    public ResponseEntity<Page<VendorResponseDto>> listVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listVendors(PageRequest.of(page, size)));
    }

    @Operation(summary = "Delete a vendor")
    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        adminService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all delivery partners")
    @GetMapping("/delivery-partners")
    public ResponseEntity<Page<DeliveryPartnerResponseDto>> listDeliveryPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listDeliveryPartners(PageRequest.of(page, size)));
    }

    @Operation(summary = "Delete a delivery partner")
    @DeleteMapping("/delivery-partners/{id}")
    public ResponseEntity<Void> deleteDeliveryPartner(@PathVariable Long id) {
        adminService.deleteDeliveryPartner(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all orders")
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDto>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listOrders(PageRequest.of(page, size)));
    }

    @Operation(summary = "Delete an order")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        adminService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all reviews")
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listReviews(PageRequest.of(page, size)));
    }

    @Operation(summary = "List all payments")
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentResponseDto>> listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listPayments(PageRequest.of(page, size)));
    }
}
