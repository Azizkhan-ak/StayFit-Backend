package com.example.stayfit.utility;

public enum AuthProvider {
    GOOGLE_AUTH(1, "google auth"),
    LOCAL_AUTH(2, "local user");

    private final int code;
    private final String description;

    AuthProvider(int code, String description) {
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
