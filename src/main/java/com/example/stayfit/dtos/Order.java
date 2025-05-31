package com.example.stayfit.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class Order {
    private String name;
    private Boolean useUserContact;
    private String city;
    private String country;
    private String deliveryAddress;
    private String email;
    private String phone;
    private String paymentMethod;
    private Integer userId;
    List<Product> products;
    PaymentDto paymentDto;
}
