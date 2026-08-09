package com.streeteat.streeteat.vendor.service;

import com.streeteat.streeteat.common.GeoUtils;
import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.vendor.dto.VendorRequestDto;
import com.streeteat.streeteat.vendor.dto.VendorResponseDto;
import com.streeteat.streeteat.vendor.model.Vendor;
import com.streeteat.streeteat.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    /**
     * A logged-in VENDOR user creates their vendor profile.
     */
    @Transactional
    public VendorResponseDto registerVendor(VendorRequestDto request, String ownerEmail) {
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A vendor with this email already exists");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        Vendor vendor = Vendor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .owner(owner)
                .active(true)
                .build();

        return toResponse(vendorRepository.save(vendor), null);
    }

    public VendorResponseDto getVendor(Long id) {
        return toResponse(findOrThrow(id), null);
    }

    public List<VendorResponseDto> listActiveVendors() {
        return vendorRepository.findByActiveTrue().stream()
                .map(v -> toResponse(v, null))
                .collect(Collectors.toList());
    }

    /**
     * Haversine-based discovery: returns active vendors within radiusKm of
     * the given point, sorted nearest-first.
     */
    public List<VendorResponseDto> findNearbyVendors(double lat, double lon, double radiusKm) {
        return vendorRepository.findByActiveTrue().stream()
                .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                .map(v -> {
                    double distance = GeoUtils.haversineDistanceKm(lat, lon, v.getLatitude(), v.getLongitude());
                    return toResponse(v, distance);
                })
                .filter(dto -> dto.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(VendorResponseDto::getDistanceKm))
                .collect(Collectors.toList());
    }

    @Transactional
    public VendorResponseDto updateVendor(Long id, VendorRequestDto request, String requesterEmail, boolean isAdmin) {
        Vendor vendor = findOrThrow(id);
        assertOwnerOrAdmin(vendor, requesterEmail, isAdmin);

        vendor.setName(request.getName());
        vendor.setPhone(request.getPhone());
        vendor.setAddress(request.getAddress());
        vendor.setLatitude(request.getLatitude());
        vendor.setLongitude(request.getLongitude());
        // email intentionally not mutated here to keep it a stable identifier

        return toResponse(vendorRepository.save(vendor), null);
    }

    @Transactional
    public void deleteVendor(Long id, String requesterEmail, boolean isAdmin) {
        Vendor vendor = findOrThrow(id);
        assertOwnerOrAdmin(vendor, requesterEmail, isAdmin);
        vendor.setActive(false); // soft delete, consistent with is_active column
        vendorRepository.save(vendor);
    }

    // ---------- helpers ----------

    public Vendor findOrThrow(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
    }

    private void assertOwnerOrAdmin(Vendor vendor, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (vendor.getOwner() == null || !vendor.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new BadRequestException("You do not own this vendor profile");
        }
    }

    public VendorResponseDto toResponseDto(Vendor vendor) {
        return toResponse(vendor, null);
    }

    private VendorResponseDto toResponse(Vendor vendor, Double distanceKm) {
        return VendorResponseDto.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .address(vendor.getAddress())
                .latitude(vendor.getLatitude())
                .longitude(vendor.getLongitude())
                .distanceKm(distanceKm)
                .active(vendor.isActive())
                .createdAt(vendor.getCreatedAt())
                .build();
    }
}
