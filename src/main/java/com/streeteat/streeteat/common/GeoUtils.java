package com.streeteat.streeteat.common;

/**
 * Geolocation utilities.
 * <p>
 * Implements the Haversine formula to compute the great-circle distance
 * between two lat/lng points on Earth. Used for:
 *  - customer-facing "vendors near me" discovery within a configurable radius
 *  - assigning the nearest available delivery partner to a ready order
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    /**
     * Returns the great-circle distance between two points, in kilometers.
     */
    public static double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        return haversineDistanceKm(lat1, lon1, lat2, lon2) <= radiusKm;
    }
}
