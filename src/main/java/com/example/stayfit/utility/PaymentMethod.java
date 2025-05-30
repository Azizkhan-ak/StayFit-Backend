package com.example.stayfit.utility;

public enum PaymentMethod {
    ONLINE(1,"PAY ONLINE VIA CREDIT/DEBIT CARD"),CASHONDELIVERY(2,"PAY ON DELIVERY VIA CASH");

    Integer id;
    String desc;

    PaymentMethod(Integer id,String desc){
        this.desc=desc;
        this.id= id;
    }

    public Integer getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
