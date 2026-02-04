package com.streeteat.streeteat.order.service;

import com.streeteat.streeteat.delivery.model.Delivery;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.repository.DeliveryRepository;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.dto.OrderRequestDto;
import com.streeteat.streeteat.order.dto.OrderResponseDto;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.model.OrderStatus;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * CUSTOMER places an order
     * → Order is created
     * → Delivery entry is auto-created
     */
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto request, String username) {

        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = Order.builder()
                .customer(customer)
                .vendorId(request.getVendorId())
                .items(request.getItems())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 🔗 ORDER → DELIVERY INTEGRATION (IMPORTANT)
        Delivery delivery = Delivery.builder()
                .orderId(savedOrder.getId())
                .status(DeliveryStatus.PENDING)
                .build();

        deliveryRepository.save(delivery);

        return mapToResponse(savedOrder);
    }

    /**
     * CUSTOMER / ADMIN / VENDOR can view order
     */
    public OrderResponseDto getOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    /**
     * CUSTOMER: view own orders
     */
    public Page<OrderResponseDto> getOrdersForCustomer(String username, Pageable pageable) {
        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByCustomer(customer, pageable)
                .map(this::mapToResponse);
    }

    /**
     * VENDOR: view orders for vendor
     */
    public Page<OrderResponseDto> getOrdersForVendor(Long vendorId, Pageable pageable) {
        return orderRepository.findByVendorId(vendorId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * VENDOR accepts order
     */
    @Transactional
    public OrderResponseDto vendorAcceptOrder(Long orderId, Long vendorId) {
        Order order = getVendorOrder(orderId, vendorId);
        order.setStatus(OrderStatus.ACCEPTED);
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * VENDOR rejects order
     */
    @Transactional
    public OrderResponseDto vendorRejectOrder(Long orderId, Long vendorId, String reason) {
        Order order = getVendorOrder(orderId, vendorId);
        order.setStatus(OrderStatus.REJECTED);
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * VENDOR marks order ready → delivery can start
     */
    @Transactional
    public OrderResponseDto markReady(Long orderId, Long vendorId) {
        Order order = getVendorOrder(orderId, vendorId);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        return mapToResponse(orderRepository.save(order));
    }

    // ---------- HELPER METHODS ----------

    private Order getVendorOrder(Long orderId, Long vendorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized vendor access");
        }
        return order;
    }

    private OrderResponseDto mapToResponse(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .vendorId(order.getVendorId())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
