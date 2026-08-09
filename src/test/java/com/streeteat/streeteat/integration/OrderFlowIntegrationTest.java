package com.streeteat.streeteat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void customerCanRegisterLoginAndPlaceOrderWithVendorAndMenu() throws Exception {
        // 1. Register a vendor-owning user
        String vendorOwnerJson = objectMapper.writeValueAsString(Map.of(
                "name", "Bob Vendor",
                "email", "bob.vendor@test.com",
                "password", "password123",
                "role", "VENDOR"
        ));
        String vendorToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendorOwnerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        // 2. Vendor registers their vendor profile with coordinates
        String vendorPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Test Street Kitchen",
                "email", "kitchen@test.com",
                "phone", "9876543210",
                "address", "Test Address",
                "latitude", 28.6139,
                "longitude", 77.2090
        ));
        String vendorResp = mockMvc.perform(post("/api/vendors")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendorPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long vendorId = ((Number) objectMapper.readValue(vendorResp, Map.class).get("id")).longValue();

        // 3. Vendor adds a menu item (price = 120.00)
        String menuItemPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Paneer Wrap",
                "price", 120.0
        ));
        String menuResp = mockMvc.perform(post("/api/vendors/" + vendorId + "/menu")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(menuItemPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Paneer Wrap"))
                .andReturn().getResponse().getContentAsString();
        Long menuItemId = ((Number) objectMapper.readValue(menuResp, Map.class).get("id")).longValue();

        // 4. Nearby search finds the vendor (Haversine within a generous radius)
        mockMvc.perform(get("/api/vendors/nearby")
                        .param("lat", "28.6100")
                        .param("lng", "77.2050")
                        .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Street Kitchen"));

        // 5. Register a customer and place an order for 2x the menu item.
        // totalAmount is NOT sent by the client -- it must be computed
        // server-side as price(120.00) * quantity(2) = 240.00.
        String customerJson = objectMapper.writeValueAsString(Map.of(
                "name", "Alice Customer",
                "email", "alice.customer@test.com",
                "password", "password123",
                "role", "CUSTOMER"
        ));
        String customerToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        String orderPayload = objectMapper.writeValueAsString(Map.of(
                "vendorId", vendorId,
                "items", List.of(Map.of("menuItemId", menuItemId, "quantity", 2))
        ));
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(240.0))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].menuItemName").value("Paneer Wrap"));
    }

    @Test
    void serverIgnoresClientSuppliedPriceEvenIfSmuggledIntoTheRequest() throws Exception {
        // Set up a vendor + menu item priced at 100.00
        String vendorOwnerJson = objectMapper.writeValueAsString(Map.of(
                "name", "Carol Vendor",
                "email", "carol.vendor@test.com",
                "password", "password123",
                "role", "VENDOR"
        ));
        String vendorToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendorOwnerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        String vendorPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Tamper Test Kitchen",
                "email", "tampertest@test.com",
                "latitude", 28.6, "longitude", 77.2
        ));
        String vendorResp = mockMvc.perform(post("/api/vendors")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendorPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long vendorId = ((Number) objectMapper.readValue(vendorResp, Map.class).get("id")).longValue();

        String menuResp = mockMvc.perform(post("/api/vendors/" + vendorId + "/menu")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Samosa", "price", 100.0))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long menuItemId = ((Number) objectMapper.readValue(menuResp, Map.class).get("id")).longValue();

        String customerToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Mallory Customer", "email", "mallory@test.com",
                                "password", "password123", "role", "CUSTOMER"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        // Attempt to smuggle a totalAmount of 1.00 for 3x a 100.00 item (would be
        // 300.00 if computed honestly). The DTO has no totalAmount/price field at
        // all, so this extra JSON property is simply ignored by Jackson.
        String tamperedPayload = objectMapper.writeValueAsString(Map.of(
                "vendorId", vendorId,
                "totalAmount", 1.00,
                "items", List.of(Map.of("menuItemId", menuItemId, "quantity", 3, "price", 1.00))
        ));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(300.0)); // 100.00 * 3, server-computed, not 1.00
    }

    @SuppressWarnings("unchecked")
    private String extractToken(String jsonResponse) throws Exception {
        Map<String, Object> map = objectMapper.readValue(jsonResponse, Map.class);
        return (String) map.get("token");
    }
}
