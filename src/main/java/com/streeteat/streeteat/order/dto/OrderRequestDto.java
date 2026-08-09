package com.streeteat.streeteat.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    @NotNull(message = "vendorId is required")
    private Long vendorId;

    // Replaces the old free-text "items" string + client-supplied totalAmount.
    // The server looks up each menu item's real price and computes the total
    // itself (see OrderService.placeOrder) -- a client-supplied price/total
    // was a genuine tampering vulnerability in the earlier version of this API.
    @NotEmpty(message = "order must contain at least one item")
    @Valid
    private List<OrderItemRequestDto> items;
}
