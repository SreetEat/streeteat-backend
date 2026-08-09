package com.streeteat.streeteat.order.service;

import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.delivery.model.Delivery;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.repository.DeliveryRepository;
import com.streeteat.streeteat.delivery.service.DeliveryPartnerService;
import com.streeteat.streeteat.menu.model.MenuItem;
import com.streeteat.streeteat.menu.repository.MenuItemRepository;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.dto.OrderItemRequestDto;
import com.streeteat.streeteat.order.dto.OrderItemResponseDto;
import com.streeteat.streeteat.order.dto.OrderRequestDto;
import com.streeteat.streeteat.order.dto.OrderResponseDto;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.model.OrderItem;
import com.streeteat.streeteat.order.model.OrderStatus;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.vendor.model.Vendor;
import com.streeteat.streeteat.vendor.repository.VendorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * CUSTOMER places an order
     * -> Each requested line is resolved against the vendor's real menu items;
     *    price is taken from MenuItem.price server-side, never from the client
     *    (a client-supplied totalAmount was a real price-tampering vulnerability
     *    in the earlier version of this API).
     * -> Order + OrderItems are created
     * -> Delivery entry is auto-created
     */
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto request, String username) {

        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + request.getVendorId()));

        if (!vendor.isActive()) {
            throw new BadRequestException("Vendor is not currently accepting orders");
        }

        Order order = Order.builder()
                .customer(customer)
                .vendor(vendor)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDto line : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(line.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + line.getMenuItemId()));

            if (!menuItem.getVendor().getId().equals(vendor.getId())) {
                throw new BadRequestException("Menu item " + menuItem.getId() + " does not belong to vendor " + vendor.getId());
            }
            if (!menuItem.isAvailable()) {
                throw new BadRequestException("Menu item '" + menuItem.getName() + "' is currently unavailable");
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(line.getQuantity())
                    .price(menuItem.getPrice()) // server-computed snapshot, not client-supplied
                    .build();
            order.addItem(orderItem);

            total = total.add(orderItem.lineTotal());
        }
        order.setTotalAmount(total);

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
     * CUSTOMER / ADMIN / VENDOR can view order -- but only their own (customer
     * who placed it, vendor who owns it, or admin). Previously this had no
     * ownership check at all, so any authenticated user could view any order
     * just by guessing its id.
     */
    public OrderResponseDto getOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        boolean isCustomer = order.getCustomer().getEmail().equalsIgnoreCase(username);
        boolean isVendorOwner = order.getVendor().getOwner() != null
                && order.getVendor().getOwner().getEmail().equalsIgnoreCase(username);
        boolean isAdmin = userRepository.findByEmail(username)
                .map(u -> u.getRole() == com.streeteat.streeteat.model.Role.ADMIN)
                .orElse(false);

        if (!isCustomer && !isVendorOwner && !isAdmin) {
            throw new BadRequestException("You do not have access to this order");
        }

        return mapToResponse(order);
    }

    /**
     * CUSTOMER: view own orders
     */
    public Page<OrderResponseDto> getOrdersForCustomer(String username, Pageable pageable) {
        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByCustomer(customer, pageable)
                .map(this::mapToResponse);
    }

    /**
     * VENDOR: view orders for the authenticated vendor's own profile.
     * Admin should use /api/admin/orders instead -- there's no "admin's own
     * vendor" to resolve here.
     */
    public Page<OrderResponseDto> getOrdersForVendor(String username, Pageable pageable) {
        Long vendorId = resolveOwnedVendorId(username, false, null);
        return orderRepository.findByVendorId(vendorId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * VENDOR accepts order. vendorId is resolved from the authenticated
     * user's own Vendor profile -- previously this trusted a client-supplied
     * vendorId query parameter, meaning any VENDOR-role user could accept or
     * reject orders belonging to a vendor they don't actually own.
     */
    @Transactional
    public OrderResponseDto vendorAcceptOrder(Long orderId, String username, boolean isAdmin) {
        Order order = getVendorOwnedOrder(orderId, username, isAdmin);
        order.setStatus(OrderStatus.ACCEPTED);
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * VENDOR rejects order
     */
    @Transactional
    public OrderResponseDto vendorRejectOrder(Long orderId, String username, boolean isAdmin, String reason) {
        Order order = getVendorOwnedOrder(orderId, username, isAdmin);
        order.setStatus(OrderStatus.REJECTED);
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * VENDOR marks order ready → delivery starts, and the nearest available
     * delivery partner is auto-assigned using the Haversine formula against
     * the vendor's coordinates.
     */
    @Transactional
    public OrderResponseDto markReady(Long orderId, String username, boolean isAdmin) {
        Order order = getVendorOwnedOrder(orderId, username, isAdmin);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        deliveryPartnerService.assignNearestPartnerToOrder(order)
                .ifPresent(order::setDeliveryPartner);

        return mapToResponse(orderRepository.save(order));
    }

    // ---------- HELPER METHODS ----------

    /**
     * Resolves the vendorId this request is allowed to act as: the
     * authenticated user's own Vendor profile, or -- for admin -- the
     * expectedVendorId of the order being acted on (admin has no "own"
     * vendor, so it can't be resolved from ownership).
     */
    private Long resolveOwnedVendorId(String username, boolean isAdmin, Long expectedVendorId) {
        if (isAdmin) {
            return expectedVendorId;
        }
        return vendorRepository.findByOwnerId(
                        userRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                                .getId())
                .orElseThrow(() -> new BadRequestException("This account has no vendor profile"))
                .getId();
    }

    private Order getVendorOwnedOrder(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (isAdmin) {
            return order;
        }

        Long ownedVendorId = resolveOwnedVendorId(username, false, null);
        if (!order.getVendor().getId().equals(ownedVendorId)) {
            throw new BadRequestException("Unauthorized vendor access");
        }
        return order;
    }

    public OrderResponseDto toResponseDto(Order order) {
        return mapToResponse(order);
    }

    private OrderResponseDto mapToResponse(Order order) {
        List<OrderItemResponseDto> items = order.getOrderItems().stream()
                .map(oi -> OrderItemResponseDto.builder()
                        .menuItemId(oi.getMenuItem().getId())
                        .menuItemName(oi.getMenuItem().getName())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .lineTotal(oi.lineTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .vendorId(order.getVendor().getId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
