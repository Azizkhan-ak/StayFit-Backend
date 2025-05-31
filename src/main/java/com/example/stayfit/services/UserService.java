package com.example.stayfit.services;

import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;

public interface UserService {

    public ResponseDto signUp(UserDto userDto);
    public ResponseDto signIn(UserDto userDto);
}
