package com.example.stayfit.controllers;

import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "order.json")
@Slf4j
@CrossOrigin("http://localhost:5173")
public class OrderController {

    OrderService orderService;

    @Autowired
    private OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping(value = "/placeOrder")
    public ResponseDto placeOrder(@RequestBody Order order){
        ResponseDto responseDto = null;
        log.info("==============Placing order =============");
        responseDto = orderService.placeOrder(order);
        log.info("==========================================");
        return responseDto;
    }

}
