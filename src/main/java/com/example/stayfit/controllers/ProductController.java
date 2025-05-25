package com.example.stayfit.controllers;

import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.ProductsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("products.json")
@CrossOrigin("http://localhost:5173")
public class ProductController {

    ProductsService productsService;

    @Autowired
    public ProductController(ProductsService productsService){
        this.productsService = productsService;
    }

    @GetMapping(value = "/getProducts")
    public ResponseDto getProducts(@RequestParam(value = "category" ) Integer category){
        log.info("============ Get All Products =============");
        ResponseDto responseDto = productsService.getAllProducts(category);
        return responseDto;
    }
}
