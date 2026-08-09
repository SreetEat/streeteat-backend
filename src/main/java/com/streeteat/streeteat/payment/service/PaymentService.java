package com.streeteat.streeteat.payment.service;

import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.payment.dto.PaymentRequestDto;
import com.streeteat.streeteat.payment.dto.PaymentResponseDto;
import com.streeteat.streeteat.payment.model.Payment;
import com.streeteat.streeteat.payment.model.PaymentMethod;
import com.streeteat.streeteat.payment.model.PaymentStatus;
import com.streeteat.streeteat.payment.repository.PaymentRepository;
import com.streeteat.streeteat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IMPORTANT: this is a payment LEDGER, not a real payment gateway
 * integration. There is no connection to Razorpay/Stripe/UPI rails or any
 * other real processor -- that requires provider credentials and a live
 * account, which is out of scope for me to set up. CARD/UPI payments here
 * are recorded as SUCCESS immediately, which is only appropriate for a demo/
 * portfolio project. Before this touches real money, this class is exactly
 * where a real gateway SDK call needs to replace the current stub logic.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponseDto recordPayment(PaymentRequestDto request, String username) {
        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("You can only pay for your own orders");
        }
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("This order already has a payment recorded");
        }

        // Stub logic: COD is always "PENDING" until delivery; CARD/UPI are
        // recorded as immediately SUCCESSFUL since there's no real gateway
        // behind this call. Replace this branch with an actual gateway
        // call + webhook-driven status update for production use.
        PaymentStatus status = request.getMethod() == PaymentMethod.CASH_ON_DELIVERY
                ? PaymentStatus.PENDING
                : PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(request.getMethod())
                .status(status)
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponseDto getPaymentForOrder(Long orderId, String username, boolean isAdmin) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment recorded for order: " + orderId));

        if (!isAdmin && !payment.getOrder().getCustomer().getEmail().equalsIgnoreCase(username)) {
            throw new BadRequestException("You do not have access to this order's payment");
        }

        return toResponse(payment);
    }

    public PaymentResponseDto toResponseDto(Payment payment) {
        return toResponse(payment);
    }

    private PaymentResponseDto toResponse(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
