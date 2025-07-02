package com.example.stayfit.services;

import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;

public interface AuthenticationService {
    public ResponseDto login(UserDto userDto);
    public ResponseDto register(UserDto userDto);
    public ResponseDto verifyEmail(String oneTimeToken,String email);

    public ResponseDto sendPasswordResetEmail(String email);

    public ResponseDto passwordReset(String token,String password);
}
