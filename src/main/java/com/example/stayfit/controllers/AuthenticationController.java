package com.example.stayfit.controllers;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;
import com.example.stayfit.services.AuthenticationService;
import com.example.stayfit.utility.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;


@RestController
@RequestMapping("/api/public")
@CrossOrigin("http://localhost:5173")
@Slf4j
public class AuthenticationController {

    private AuthenticationService authenticate;

    private EmailHandler emailHandler;
    @Autowired
    AuthenticationController(AuthenticationService authenticate,EmailHandler emailHandler) {
        this.authenticate = authenticate;
        this.emailHandler = emailHandler;
    }

    @PostMapping("/login")
    public ResponseDto login(@RequestBody UserDto request) {
        ResponseDto responseDto = null;
        try {
            responseDto = authenticate.login(request);
            return responseDto;
        } catch (Exception e) {
            emailHandler.sendErrorEmail(Constants.exceptionSubject, e.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
    }


    @PostMapping("/register")
    public ResponseDto register(@Valid @RequestBody UserDto request) throws Exception {
        ResponseDto responseDto = null;
        try{
            responseDto = authenticate.register(new UserDto());
            return responseDto;
        }catch (Exception e){
            emailHandler.sendErrorEmail(Constants.exceptionSubject, e.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
    }

    @GetMapping(value = "/passwordResetEmail")
    public ResponseDto passwordResetEmail(@RequestParam(value = "email")String email){
        ResponseDto responseDto = null;
        try{
            responseDto = authenticate.sendPasswordResetEmail(email);
            return responseDto;
        }catch (Exception ex){
            emailHandler.sendErrorEmail(Constants.exceptionSubject, ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
    }

    @GetMapping(value = "/passwordReset")
    public ResponseDto passwordReset(@RequestParam(value = "token")String token,@RequestParam(value = "password") String password){
        ResponseDto responseDto = null;
        try{
            responseDto = authenticate.passwordReset(token,password);
            return responseDto;
        }catch (Exception ex){
            emailHandler.sendErrorEmail(Constants.exceptionSubject, ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
    }

    @GetMapping(value = "/verifyemail")
    public ResponseDto verifyEmail(@RequestParam(value = "token") String token,@RequestParam(value = "email")String email){
        ResponseDto responseDto = null;
        try{
            responseDto = authenticate.verifyEmail(token,email);
            return responseDto;
        }catch (Exception ex){
            emailHandler.sendErrorEmail(Constants.exceptionSubject, ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
    }

}
