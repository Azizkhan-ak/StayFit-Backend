package com.example.stayfit.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDto {
    private String paymentIntentId;
    private Float amount;
    private String created;
    private String currency;
}
