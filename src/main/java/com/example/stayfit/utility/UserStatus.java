package com.example.stayfit.utility;

public enum UserStatus {
    INACTIVE(0, "Inactive"),
    ACTIVE(1, "Active"),
    DELETED(6, "Deleted");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
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
