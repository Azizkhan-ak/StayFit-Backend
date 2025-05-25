package com.example.stayfit.services;

import com.example.stayfit.dtos.ResponseDto;
import org.springframework.stereotype.Service;

public interface ProductsService {
    public ResponseDto getAllProducts(Integer category);
}
