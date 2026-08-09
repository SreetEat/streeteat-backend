package com.streeteat.streeteat.vendor.controller;

import com.streeteat.streeteat.vendor.dto.VendorRequestDto;
import com.streeteat.streeteat.vendor.dto.VendorResponseDto;
import com.streeteat.streeteat.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor APIs", description = "Vendor registration, management and geolocation-based discovery")
public class VendorController {

    private final VendorService vendorService;

    @Operation(summary = "Register a vendor profile for the logged-in VENDOR user")
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorResponseDto> register(@Valid @RequestBody VendorRequestDto request,
                                                        Authentication auth) {
        return ResponseEntity.ok(vendorService.registerVendor(request, auth.getName()));
    }

    @Operation(summary = "List all active vendors")
    @GetMapping
    public ResponseEntity<List<VendorResponseDto>> listVendors() {
        return ResponseEntity.ok(vendorService.listActiveVendors());
    }

    @Operation(summary = "Get a vendor by id")
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDto> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendor(id));
    }

    @Operation(summary = "Find vendors near a location (Haversine formula) within a radius in km")
    @GetMapping("/nearby")
    public ResponseEntity<List<VendorResponseDto>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm
    ) {
        return ResponseEntity.ok(vendorService.findNearbyVendors(lat, lng, radiusKm));
    }

    @Operation(summary = "Update a vendor profile (owner vendor or admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<VendorResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody VendorRequestDto request,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(vendorService.updateVendor(id, request, auth.getName(), isAdmin));
    }

    @Operation(summary = "Deactivate a vendor profile (owner vendor or admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        vendorService.deleteVendor(id, auth.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
