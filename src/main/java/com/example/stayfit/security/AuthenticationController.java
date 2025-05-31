package com.example.stayfit.security;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.UserDto;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

@RestController
public class AuthenticationController {

    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private PostgresQlConfig postgresQlConfig;
    private AuthenticationManager authManager;
    private EmailHandler emailHandler;

    AuthenticationController(JwtUtil jwtUtil,PasswordEncoder passwordEncoder,PostgresQlConfig postgresQlConfig,AuthenticationManager authenticationManager,EmailHandler emailHandler){
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.postgresQlConfig = postgresQlConfig;
        this.authManager = authenticationManager;
        this.emailHandler = emailHandler;
    }

    @PostMapping("public/login")
    public ResponseEntity<?> login(@RequestBody UserDto request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
    }


    @PostMapping("public/register")
    public String register(@RequestBody UserDto request) throws Exception {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = postgresQlConfig.getConnection();
            PreparedStatement ps = connection.prepareStatement("insert into public.users (id,created_at,status,modified_at,role,first_name,last_name,email,password,city,country,delivery_address,phone) " +
                    "VALUES (nextval('users_id_seq'),?,?,?,?,?,?,?,?,?,?,?,?) ");
            ps.setTimestamp(1,  new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, UserStatus.INACTIVE.getCode());
            ps.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
            ps.setInt(4,0); // 0 role for customer
            ps.setString(5,request.getFirstName());
            ps.setString(6,request.getLastName());
            ps.setString(7,request.getEmail());
            ps.setString(8,passwordEncoder.encode(request.getPassword()));
            ps.setString(9,request.getCity());
            ps.setString(10,request.getCountry());
            ps.setString(11,request.getAddress());
            ps.setString(12,request.getPhone());
            ps.executeUpdate();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            try {

            }catch (Exception ex){
                ex.printStackTrace();
                emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            }
        }
        return "User registered successfully";
    }


}
