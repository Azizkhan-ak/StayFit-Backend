package com.example.stayfit.services;

import com.example.stayfit.dtos.ResponseDto;

public interface AdminService {
    public ResponseDto listInventory(String token);
    public ResponseDto deleteInventoryItem(String token,Integer itemId);
}
