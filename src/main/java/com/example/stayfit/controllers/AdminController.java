package com.example.stayfit.controllers;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.AdminService;
import com.example.stayfit.utility.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "admin.json")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = {"Authorization", "Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        exposedHeaders = "Authorization"
)
public class AdminController {

    private EmailHandler emailHandler;
    private AdminService adminService;

    @Autowired
    public AdminController(EmailHandler emailHandler,AdminService adminService){
        this.emailHandler = emailHandler;
        this.adminService = adminService;
    }

    @GetMapping(value = "/inventory")
    public ResponseDto listInventory(
            @RequestHeader(value = "Authorization") String token
    ){
        ResponseDto responseDto = null;
        try{
            responseDto = adminService.listInventory(token);
        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,true);
        }
        return responseDto;
    }

    @DeleteMapping(value = "/inventory/delete")
    public ResponseDto deleteInventoryItem(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam(value = "itemId") Integer id
    ){
        ResponseDto responseDto = null;
        try{
            responseDto = adminService.deleteInventoryItem(token,id);
        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,true);
        }
        return responseDto;
    }
}
