package com.streeteat.streeteat.delivery.service;

import com.streeteat.streeteat.common.GeoUtils;
import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.delivery.dto.DeliveryPartnerRequestDto;
import com.streeteat.streeteat.delivery.dto.DeliveryPartnerResponseDto;
import com.streeteat.streeteat.delivery.dto.DeliveryResponseDto;
import com.streeteat.streeteat.delivery.model.Delivery;
import com.streeteat.streeteat.delivery.model.DeliveryPartner;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.model.PartnerStatus;
import com.streeteat.streeteat.delivery.repository.DeliveryPartnerRepository;
import com.streeteat.streeteat.delivery.repository.DeliveryRepository;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private static final double FALLBACK_EARNING_PER_DELIVERY = 50.0;

    private final DeliveryPartnerRepository partnerRepo;
    private final DeliveryRepository deliveryRepo;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ---------- CRUD ----------

    @Transactional
    public DeliveryPartnerResponseDto registerPartner(DeliveryPartnerRequestDto request, String ownerEmail) {
        if (partnerRepo.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A delivery partner with this email already exists");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        DeliveryPartner partner = DeliveryPartner.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .vehicle(request.getVehicle())
                .area(request.getArea())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .owner(owner)
                .status(PartnerStatus.OFFLINE) // starts offline until the partner explicitly goes available
                .active(true)
                .build();

        return toResponse(partnerRepo.save(partner));
    }

    public DeliveryPartnerResponseDto getPartner(Long id) {
        return toResponse(findOrThrow(id));
    }

    public Page<DeliveryPartnerResponseDto> listActivePartners(Pageable pageable) {
        return partnerRepo.findByActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional
    public DeliveryPartnerResponseDto updatePartner(Long id, DeliveryPartnerRequestDto request, String requesterEmail, boolean isAdmin) {
        DeliveryPartner partner = findOrThrow(id);
        assertOwnerOrAdmin(partner, requesterEmail, isAdmin);

        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setVehicle(request.getVehicle());
        partner.setArea(request.getArea());
        partner.setLatitude(request.getLatitude());
        partner.setLongitude(request.getLongitude());
        // email intentionally not mutated here to keep it a stable identifier

        return toResponse(partnerRepo.save(partner));
    }

    /**
     * Self-service availability toggle: a partner sets themself AVAILABLE or
     * OFFLINE. Only meaningful for the two states a human controls directly --
     * ON_DELIVERY is set by the system when a delivery is actually assigned.
     */
    @Transactional
    public DeliveryPartnerResponseDto updateAvailability(Long id, PartnerStatus status, String requesterEmail, boolean isAdmin) {
        if (status == PartnerStatus.ON_DELIVERY) {
            throw new BadRequestException("ON_DELIVERY is set automatically by the system, not chosen directly");
        }
        DeliveryPartner partner = findOrThrow(id);
        assertOwnerOrAdmin(partner, requesterEmail, isAdmin);
        partner.setStatus(status);
        return toResponse(partnerRepo.save(partner));
    }

    @Transactional
    public void deletePartner(Long id, String requesterEmail, boolean isAdmin) {
        DeliveryPartner partner = findOrThrow(id);
        assertOwnerOrAdmin(partner, requesterEmail, isAdmin);
        partner.setActive(false); // soft delete, consistent with is_active column
        partner.setStatus(PartnerStatus.OFFLINE);
        partnerRepo.save(partner);
    }

    // ---------- EXISTING DELIVERY-ACTION LOGIC ----------

    /**
     * Only the delivery partner actually assigned to this delivery (or
     * admin) may act on it. Previously any authenticated DELIVERY_PARTNER
     * could accept/reject/update ANY delivery, not just their own.
     */
    public DeliveryResponseDto updateDeliveryAction(Long deliveryId, String action, String requesterEmail, boolean isAdmin) {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));
        assertAssignedPartnerOrAdmin(delivery, requesterEmail, isAdmin);

        if (action.equalsIgnoreCase("ACCEPT")) {
            delivery.setStatus(DeliveryStatus.ACCEPTED);
        } else if (action.equalsIgnoreCase("REJECT")) {
            delivery.setStatus(DeliveryStatus.REJECTED);
        } else {
            throw new BadRequestException("Unknown delivery action: " + action);
        }
        return toResponse(deliveryRepo.save(delivery));
    }

    public DeliveryResponseDto updateDeliveryStatus(Long deliveryId, DeliveryStatus status, String requesterEmail, boolean isAdmin) {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));
        assertAssignedPartnerOrAdmin(delivery, requesterEmail, isAdmin);
        delivery.setStatus(status);

        if (status == DeliveryStatus.DELIVERED) {
            DeliveryPartner partner = delivery.getDeliveryPartner();
            if (partner != null) {
                partner.setEarnings(partner.getEarnings() + FALLBACK_EARNING_PER_DELIVERY);
                partner.setStatus(PartnerStatus.AVAILABLE);
                partnerRepo.save(partner);
            }
            // Sync the parent Order -- Order and Delivery status were previously
            // tracked completely independently, so an order could never actually
            // reach DELIVERED through the normal flow (silently blocking anything
            // gated on it, like customer reviews).
            orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
                order.setStatus(com.streeteat.streeteat.order.model.OrderStatus.DELIVERED);
                orderRepository.save(order);
            });
        } else if (status == DeliveryStatus.PICKED_UP) {
            DeliveryPartner partner = delivery.getDeliveryPartner();
            if (partner != null) {
                partner.setStatus(PartnerStatus.ON_DELIVERY);
                partnerRepo.save(partner);
            }
        }

        return toResponse(deliveryRepo.save(delivery));
    }

    public double getEarnings(Long partnerId, String requesterEmail, boolean isAdmin) {
        DeliveryPartner partner = findOrThrow(partnerId);
        assertOwnerOrAdmin(partner, requesterEmail, isAdmin);
        return partner.getEarnings();
    }

    /**
     * Delivery assignment logic (Day-4 task item).
     * Picks the nearest AVAILABLE, active delivery partner to the vendor's
     * location using the Haversine formula, marks them ON_DELIVERY, and
     * links the Delivery record created at order-placement time to them.
     */
    @Transactional
    public Optional<DeliveryPartner> assignNearestPartnerToOrder(Order order) {
        if (order.getVendor() == null
                || order.getVendor().getLatitude() == null
                || order.getVendor().getLongitude() == null) {
            return Optional.empty(); // vendor has no coordinates set; can't compute distance
        }

        double vendorLat = order.getVendor().getLatitude();
        double vendorLon = order.getVendor().getLongitude();

        List<DeliveryPartner> candidates = partnerRepo.findByStatusAndActiveTrue(PartnerStatus.AVAILABLE);

        Optional<DeliveryPartner> nearest = candidates.stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .min(Comparator.comparingDouble(p ->
                        GeoUtils.haversineDistanceKm(vendorLat, vendorLon, p.getLatitude(), p.getLongitude())));

        nearest.ifPresent(partner -> {
            partner.setStatus(PartnerStatus.ON_DELIVERY);
            partnerRepo.save(partner);

            deliveryRepo.findByOrderId(order.getId())
                    .ifPresent(delivery -> {
                        delivery.setDeliveryPartner(partner);
                        delivery.setStatus(DeliveryStatus.ACCEPTED);
                        deliveryRepo.save(delivery);
                    });
        });

        return nearest;
    }

    // ---------- helpers ----------

    private DeliveryPartner findOrThrow(Long id) {
        return partnerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found: " + id));
    }

    private void assertOwnerOrAdmin(DeliveryPartner partner, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (partner.getOwner() == null || !partner.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new BadRequestException("You do not own this delivery partner profile");
        }
    }

    private void assertAssignedPartnerOrAdmin(Delivery delivery, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        DeliveryPartner partner = delivery.getDeliveryPartner();
        if (partner == null || partner.getOwner() == null || !partner.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new BadRequestException("You are not the delivery partner assigned to this delivery");
        }
    }

    private DeliveryResponseDto toResponse(Delivery delivery) {
        return DeliveryResponseDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .partnerId(delivery.getDeliveryPartner() != null ? delivery.getDeliveryPartner().getId() : null)
                .status(delivery.getStatus())
                .build();
    }

    public DeliveryPartnerResponseDto toResponseDto(DeliveryPartner partner) {
        return toResponse(partner);
    }

    private DeliveryPartnerResponseDto toResponse(DeliveryPartner partner) {
        return DeliveryPartnerResponseDto.builder()
                .id(partner.getId())
                .name(partner.getName())
                .email(partner.getEmail())
                .phone(partner.getPhone())
                .vehicle(partner.getVehicle())
                .area(partner.getArea())
                .status(partner.getStatus())
                .latitude(partner.getLatitude())
                .longitude(partner.getLongitude())
                .active(partner.isActive())
                .earnings(partner.getEarnings())
                .build();
    }
}
