package com.streeteat.streeteat.delivery.controller;

import com.streeteat.streeteat.delivery.dto.DeliveryPartnerRequestDto;
import com.streeteat.streeteat.delivery.dto.DeliveryPartnerResponseDto;
import com.streeteat.streeteat.delivery.dto.DeliveryResponseDto;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.model.PartnerStatus;
import com.streeteat.streeteat.delivery.service.DeliveryPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Consolidated delivery + delivery-partner API. Previously split across two
 * separate controllers both mapped to /api/delivery (DeliveryController,
 * which only had a health check, and this one) -- merged here to avoid two
 * classes silently owning the same base path.
 */
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner APIs", description = "Delivery partner registration/CRUD and delivery status management")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryService;

    @GetMapping("/health")
    public String healthCheck() {
        return "Delivery Service is up!";
    }

    // ---------- Delivery Partner CRUD ----------

    @Operation(summary = "Register the logged-in user as a delivery partner")
    @PostMapping("/partners")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerResponseDto> registerPartner(
            @Valid @RequestBody DeliveryPartnerRequestDto request,
            Authentication auth
    ) {
        return ResponseEntity.ok(deliveryService.registerPartner(request, auth.getName()));
    }

    @Operation(summary = "List active delivery partners")
    @GetMapping("/partners")
    public ResponseEntity<Page<DeliveryPartnerResponseDto>> listPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deliveryService.listActivePartners(PageRequest.of(page, size)));
    }

    @Operation(summary = "Get a delivery partner by id")
    @GetMapping("/partners/{id}")
    public ResponseEntity<DeliveryPartnerResponseDto> getPartner(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getPartner(id));
    }

    @Operation(summary = "Update a delivery partner profile (owner partner or admin)")
    @PutMapping("/partners/{id}")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponseDto> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryPartnerRequestDto request,
            Authentication auth
    ) {
        return ResponseEntity.ok(deliveryService.updatePartner(id, request, auth.getName(), isAdmin(auth)));
    }

    @Operation(summary = "Set availability (AVAILABLE/OFFLINE) for a delivery partner (owner partner or admin)")
    @PatchMapping("/partners/{id}/availability")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponseDto> updateAvailability(
            @PathVariable Long id,
            @RequestParam PartnerStatus status,
            Authentication auth
    ) {
        return ResponseEntity.ok(deliveryService.updateAvailability(id, status, auth.getName(), isAdmin(auth)));
    }

    @Operation(summary = "Deactivate a delivery partner profile (owner partner or admin)")
    @DeleteMapping("/partners/{id}")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id, Authentication auth) {
        deliveryService.deletePartner(id, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }

    // ---------- Existing delivery-action endpoints ----------

    @Operation(summary = "Accept or reject a delivery (assigned partner or admin only)")
    @PostMapping("/{deliveryId}/action")
    public DeliveryResponseDto updateAction(@PathVariable Long deliveryId, @RequestParam String action, Authentication auth) {
        return deliveryService.updateDeliveryAction(deliveryId, action, auth.getName(), isAdmin(auth));
    }

    @Operation(summary = "Update delivery status (PICKED_UP or DELIVERED) (assigned partner or admin only)")
    @PutMapping("/{deliveryId}/status")
    public DeliveryResponseDto updateStatus(@PathVariable Long deliveryId, @RequestParam DeliveryStatus status, Authentication auth) {
        return deliveryService.updateDeliveryStatus(deliveryId, status, auth.getName(), isAdmin(auth));
    }

    @Operation(summary = "Get earnings of a delivery partner (owner partner or admin only)")
    @GetMapping("/{partnerId}/earnings")
    public double getEarnings(@PathVariable Long partnerId, Authentication auth) {
        return deliveryService.getEarnings(partnerId, auth.getName(), isAdmin(auth));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
