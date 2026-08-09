package com.streeteat.streeteat.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilsTest {

    @Test
    void distanceBetweenSamePointIsZero() {
        double d = GeoUtils.haversineDistanceKm(28.6139, 77.2090, 28.6139, 77.2090);
        assertEquals(0.0, d, 0.0001);
    }

    @Test
    void distanceBetweenDelhiAndMumbaiIsRoughlyCorrect() {
        // Delhi (28.6139, 77.2090) to Mumbai (19.0760, 72.8777) is ~1150-1160 km.
        double d = GeoUtils.haversineDistanceKm(28.6139, 77.2090, 19.0760, 72.8777);
        assertTrue(d > 1100 && d < 1200, "Expected ~1150km, got " + d);
    }

    @Test
    void isWithinRadiusRespectsBoundary() {
        // Two points ~1.1km apart (roughly Connaught Place to India Gate, Delhi)
        double lat1 = 28.6315, lon1 = 77.2167;
        double lat2 = 28.6129, lon2 = 77.2295;

        assertTrue(GeoUtils.isWithinRadius(lat1, lon1, lat2, lon2, 5));
        assertTrue(!GeoUtils.isWithinRadius(lat1, lon1, lat2, lon2, 0.1));
    }
}
