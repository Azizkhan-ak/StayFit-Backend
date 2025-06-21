package com.example.stayfit.utility;

public enum ProductStatus {
    AVAILABLE(0, "available"),
    OUT_OF_STOCK(1, "out of stock"),
    DELETED(6, "deleted");

    private final Integer code;
    private final String desc;

    ProductStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
