package com.streeteat.streeteat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Runs the real order flow against a real, disposable Postgres container --
 * with Flyway actually enabled and Hibernate's ddl-auto=validate actually
 * checking the schema, exactly like the "prod"/"staging" profiles do.
 * <p>
 * WHY THIS TEST CLASS EXISTS: {@code OrderFlowIntegrationTest} runs the same
 * kind of flow, but against H2 with {@code spring.flyway.enabled=false} and
 * {@code ddl-auto=update} (see application-test.properties). That means it
 * never actually executes a single line of db/migration/*.sql, and never
 * validates that the JPA entities match what those migrations create.
 * <p>
 * That gap is not theoretical: an actual manual docker-compose run against
 * real Postgres surfaced five real bugs that every previous H2-based test run
 * had missed entirely -- V2's vendor insert was missing a NOT NULL email
 * column, V8 referenced a phantom order_id=50 that never existed, and three
 * separate entity fields (DeliveryPartner.earnings, DeliveryPartner.area,
 * User.phone) had no backing column at all. A fourth bug (Review.rating
 * validating as INTEGER against an actual SMALLINT column) was a Hibernate
 * validate-mode quirk invisible to H2 entirely. None of these would fail an
 * H2-based test; all of them fail immediately under this test class.
 * <p>
 * This test class turns that manual, one-off docker-compose verification
 * into something that runs automatically on every build, in CI or locally,
 * with no Docker Desktop session or manual db wipe required -- Testcontainers
 * starts and tears down a disposable Postgres for the duration of the test
 * run only.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PostgresFlywayIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("streeteat_test")
            .withUsername("streeteat_test")
            .withPassword("streeteat_test");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Mirror the prod/staging profiles exactly: Flyway owns the schema,
        // Hibernate only validates that the entities match what Flyway built.
        // This is the whole point of the test class -- see class javadoc.
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * If Flyway's migrations don't apply cleanly to a fresh Postgres, or if
     * any entity doesn't match what those migrations created, the
     * application context itself fails to start and this test never even
     * gets to make an assertion -- that failure IS the test result. This is
     * deliberately the cheapest possible test in this class: its only job is
     * to prove the schema is sound, fast, before the fuller flow test below
     * bothers exercising the API on top of it.
     */
    @Test
    void applicationContextStartsWithFlywayMigrationsAndSchemaValidationAgainstRealPostgres() {
        // Intentionally empty. Success = Spring context loaded, which means:
        //   1. All 11 Flyway migrations applied cleanly to an empty database.
        //   2. Every @Entity's mapped columns matched what those migrations
        //      created, under Hibernate's strict ddl-auto=validate.
    }

    @Test
    void customerCanRegisterLoginAndPlaceOrderAgainstRealPostgresWithFlyway() throws Exception {
        String vendorOwnerJson = objectMapper.writeValueAsString(Map.of(
                "name", "Testcontainers Vendor",
                "email", "tc.vendor@test.com",
                "password", "password123",
                "role", "VENDOR"
        ));
        String vendorToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendorOwnerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        String vendorPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Testcontainers Kitchen",
                "email", "tc.kitchen@test.com",
                "phone", "9876500000",
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

        String menuItemPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Testcontainers Wrap",
                "price", 120.0
        ));
        String menuResp = mockMvc.perform(post("/api/vendors/" + vendorId + "/menu")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(menuItemPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long menuItemId = ((Number) objectMapper.readValue(menuResp, Map.class).get("id")).longValue();

        String customerJson = objectMapper.writeValueAsString(Map.of(
                "name", "Testcontainers Customer",
                "email", "tc.customer@test.com",
                "password", "password123",
                "role", "CUSTOMER"
        ));
        String customerToken = extractToken(mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        // totalAmount is NOT sent -- must be computed server-side as
        // price(120.00) * quantity(2) = 240.00, same price-tampering
        // guarantee already covered against H2, now proven against real
        // Postgres, through real Flyway-built tables, in the same run that
        // proved the schema itself is sound.
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
                .andExpect(jsonPath("$.items[0].menuItemName").value("Testcontainers Wrap"));
    }

    @SuppressWarnings("unchecked")
    private String extractToken(String jsonResponse) throws Exception {
        Map<String, Object> map = objectMapper.readValue(jsonResponse, Map.class);
        return (String) map.get("token");
    }
}