package com.example.stayfit.utility;

public enum OrderStatus {
    INACTIVE(0, "Inactive/Removed"),
    PLACED(1, "Placed"),
    DISPATCHED(2, "Dispatched"),
    OUT_FOR_DELIVERY(3, "Out for Delivery"),
    DELIVERED(4, "Delivered");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
