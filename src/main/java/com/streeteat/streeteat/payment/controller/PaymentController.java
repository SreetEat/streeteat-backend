package com.streeteat.streeteat.payment.controller;

import com.streeteat.streeteat.payment.dto.PaymentRequestDto;
import com.streeteat.streeteat.payment.dto.PaymentResponseDto;
import com.streeteat.streeteat.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * See PaymentService for an important note: this is a ledger, not a real
 * payment gateway integration.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment APIs", description = "Payment ledger (NOT a real gateway integration -- see PaymentService)")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Record a payment for an order (customer only)")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> recordPayment(
            @Valid @RequestBody PaymentRequestDto request,
            Authentication auth
    ) {
        return ResponseEntity.ok(paymentService.recordPayment(request, auth.getName()));
    }

    @Operation(summary = "Get the payment recorded for an order (order's customer or admin only)")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentForOrder(@PathVariable Long orderId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(paymentService.getPaymentForOrder(orderId, auth.getName(), isAdmin));
    }
}
