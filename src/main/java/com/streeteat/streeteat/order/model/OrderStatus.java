package com.streeteat.streeteat.order.model;

public enum OrderStatus {
    PENDING,    // just placed by customer
    ACCEPTED,   // vendor accepted
    REJECTED,   // vendor rejected
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
