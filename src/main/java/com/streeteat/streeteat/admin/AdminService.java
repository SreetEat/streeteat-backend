package com.streeteat.streeteat.admin;

import com.streeteat.streeteat.admin.dto.AdminUserDto;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.delivery.dto.DeliveryPartnerResponseDto;
import com.streeteat.streeteat.delivery.model.DeliveryPartner;
import com.streeteat.streeteat.delivery.repository.DeliveryPartnerRepository;
import com.streeteat.streeteat.delivery.service.DeliveryPartnerService;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.dto.OrderResponseDto;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.order.service.OrderService;
import com.streeteat.streeteat.payment.dto.PaymentResponseDto;
import com.streeteat.streeteat.payment.model.Payment;
import com.streeteat.streeteat.payment.repository.PaymentRepository;
import com.streeteat.streeteat.payment.service.PaymentService;
import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.review.dto.ReviewResponseDto;
import com.streeteat.streeteat.review.model.Review;
import com.streeteat.streeteat.review.repository.ReviewRepository;
import com.streeteat.streeteat.review.service.ReviewService;
import com.streeteat.streeteat.vendor.dto.VendorResponseDto;
import com.streeteat.streeteat.vendor.model.Vendor;
import com.streeteat.streeteat.vendor.repository.VendorRepository;
import com.streeteat.streeteat.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All list* methods here return DTOs, never raw JPA entities. Returning
 * entities directly used to be a real bug waiting to happen: DeliveryPartner
 * <-> Delivery is a bidirectional relationship with no cycle-breaking
 * annotation, and with Spring's default open-in-view=true, Jackson would
 * happily walk DeliveryPartner.deliveries -> Delivery.deliveryPartner ->
 * (same partner) forever, causing a StackOverflowError the first time anyone
 * called GET /api/admin/delivery-partners.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final VendorService vendorService;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public Page<AdminUserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDto);
    }

    public Page<VendorResponseDto> listVendors(Pageable pageable) {
        return vendorRepository.findAll(pageable).map(vendorService::toResponseDto);
    }

    public Page<DeliveryPartnerResponseDto> listDeliveryPartners(Pageable pageable) {
        return deliveryPartnerRepository.findAll(pageable).map(deliveryPartnerService::toResponseDto);
    }

    public Page<OrderResponseDto> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderService::toResponseDto);
    }

    public Page<ReviewResponseDto> listReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(reviewService::toResponseDto);
    }

    public Page<PaymentResponseDto> listPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentService::toResponseDto);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userRepository.delete(user);
    }

    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
        vendorRepository.delete(vendor);
    }

    @Transactional
    public void deleteDeliveryPartner(Long id) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found: " + id));
        deliveryPartnerRepository.delete(partner);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        orderRepository.delete(order);
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
