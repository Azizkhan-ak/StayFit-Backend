package com.example.stayfit.utility;

public enum UserRole {
    CUSTOMER(0, "customer"),
    ADMIN(1, "admin");

    private final int code;
    private final String description;

    UserRole(int code, String description) {
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
