package com.example.stayfit.services;

import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.ResponseDto;

public interface OrderService {
    public ResponseDto placeOrder(Order order);

}
