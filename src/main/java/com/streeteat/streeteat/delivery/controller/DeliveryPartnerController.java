package com.streeteat.streeteat.delivery.controller;

import com.streeteat.streeteat.delivery.model.Delivery;
import com.streeteat.streeteat.delivery.model.DeliveryPartner;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.model.*;
import com.streeteat.streeteat.delivery.service.DeliveryPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner APIs", description = "APIs for managing delivery partners and their deliveries")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryService;

    @Operation(
            summary = "Register a new delivery partner",
            description = "Registers a new delivery partner with name, phone, vehicle, and area",
            requestBody = @RequestBody(
                    required = true,
                    description = "Delivery partner registration payload",
                    content = @Content(
                            schema = @Schema(implementation = DeliveryPartner.class),
                            examples = {
                                    @ExampleObject(
                                            name = "New Partner",
                                            value = "{ \"name\": \"Rahul Kumar\", \"phone\": \"9876543210\", \"vehicle\": \"Bike\", \"area\": \"Sector 62\" }"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Partner registered successfully")
            }
    )
    @PostMapping("/register")
    public DeliveryPartner registerPartner(@org.springframework.web.bind.annotation.RequestBody DeliveryPartner partner) {
        return deliveryService.registerPartner(partner);
    }

    @Operation(
            summary = "Accept or Reject a delivery",
            description = "Allows a partner to accept or reject a delivery request",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery updated successfully")
            }
    )
    @PostMapping("/{deliveryId}/action")
    public Delivery updateAction(
            @PathVariable Long deliveryId,
            @RequestParam String action
    ) {
        return deliveryService.updateDeliveryAction(deliveryId, action);
    }

    @Operation(
            summary = "Update delivery status",
            description = "Update the delivery status (PICKED_UP or DELIVERED)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery status updated")
            }
    )
    @PutMapping("/{deliveryId}/status")
    public Delivery updateStatus(
            @PathVariable Long deliveryId,
            @RequestParam DeliveryStatus status
    ) {
        return deliveryService.updateDeliveryStatus(deliveryId, status);
    }

    @Operation(
            summary = "Get earnings of a delivery partner",
            description = "Fetches the total earnings of a delivery partner",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Earnings retrieved")
            }
    )
    @GetMapping("/{partnerId}/earnings")
    public double getEarnings(@PathVariable Long partnerId) {
        return deliveryService.getEarnings(partnerId);
    }
}
