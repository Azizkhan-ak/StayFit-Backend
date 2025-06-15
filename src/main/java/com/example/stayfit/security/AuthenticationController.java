package com.example.stayfit.security;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import com.example.stayfit.utility.URLs;
import com.example.stayfit.utility.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:5173")
@Slf4j
public class AuthenticationController {

    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private PostgresQlConfig postgresQlConfig;
    private AuthenticationManager authManager;
    private EmailHandler emailHandler;

    @Autowired
    AuthenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, PostgresQlConfig postgresQlConfig, AuthenticationManager authenticationManager, EmailHandler emailHandler) {
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
    public ResponseDto register(@Valid @RequestBody UserDto request) throws Exception {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            //since the user is saved, send verfication email to verify user account
            String htmlTemplate = """
                        <html>
                          <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                            <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                              <h2 style="color: #333333;">Welcome to Stay Fit!</h2>
                              <p>Hi <strong>%s</strong>,</p>
                              <p>Thank you for signing up. Please click the button below to verify your email address and activate your account:</p>
                              <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">Verify Email</a>
                              </div>
                              <p>If the button doesn't work, you can also click the following link:</p>
                              <p><a href="%s">%s</a></p>
                              <p style="margin-top: 40px;">Thank you,<br><strong>Stay Fit Team</strong></p>
                            </div>
                          </body>
                        </html>
                    """;

            String token = UUID.randomUUID().toString();
            String username = request.getFirstName();
            String verificationLink = String.format(URLs.verfiyEmailUrl,token,request.getEmail());
            String htmlBody = String.format(htmlTemplate, username, verificationLink, verificationLink, verificationLink);
            String insertIntoEmailVerificationTokens = QueryUtil.getInsertIntoEmailVerificationQuery(request.getEmail(),token );

            connection = postgresQlConfig.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(QueryUtil.getCheckIfEmailAlreadyExists(request.getEmail()));
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                if(resultSet.getInt(3)==UserStatus.INACTIVE.getCode()){
                    //user is inactive, need to resend verification link to verify
                    preparedStatement = connection.prepareStatement(insertIntoEmailVerificationTokens);
                    preparedStatement.executeUpdate();
                    return new ResponseDto(Constants.verficationEmailResent,null,true);
                }
                else {
                    //user is active, user need to signin
                    return new ResponseDto(Constants.goToSignIn,null,false);
                }
            }

            else{
                PreparedStatement ps = connection.prepareStatement("insert into public.users (id,created_at,status,modified_at,role,first_name,last_name,email,password,city,country,delivery_address,phone) " +
                        "VALUES (nextval('users_id_seq'),?,?,?,?,?,?,?,?,?,?,?,?) ");
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, UserStatus.INACTIVE.getCode());
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, 0); // 0 role for customer
                ps.setString(5, request.getFirstName());
                ps.setString(6, request.getLastName());
                ps.setString(7, request.getEmail());
                ps.setString(8, passwordEncoder.encode(request.getPassword()));
                ps.setString(9, request.getCity());
                ps.setString(10, request.getCountry());
                ps.setString(11, request.getAddress());
                ps.setString(12, request.getPhone());
                ps.executeUpdate();

                preparedStatement = connection.prepareStatement(insertIntoEmailVerificationTokens);
                preparedStatement.executeUpdate();

                emailHandler.sendEmail(request.getEmail(),Constants.verifyEmailSubject,htmlBody);

                connection.commit();
                return new ResponseDto(Constants.verifyEmailMessage,null,true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            connection.rollback();
            return new ResponseDto(Constants.errorMessage,null,false);
        } finally {
            try {
                if(connection!=null)
                    connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                emailHandler.sendErrorEmail(Constants.exceptionSubject, ex.getMessage());
            }
        }
    }

    @GetMapping(value = "/public/verifyemail")
    public ResponseDto verifyEmail(@RequestParam(value = "token") String token,@RequestParam(value = "email")String email){

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try{
            connection = postgresQlConfig.getConnection();

            connection.setAutoCommit(false);
            String updateTokenStatus = QueryUtil.getVerifyEmailQuery();
            preparedStatement = connection.prepareStatement(updateTokenStatus);
            preparedStatement.setString(1,token);
            int rowUpdates = preparedStatement.executeUpdate();

            if(rowUpdates > 0){
                preparedStatement = connection.prepareStatement(QueryUtil.getActivateUserQuery());
                preparedStatement.setInt(1,UserStatus.ACTIVE.getCode());
                preparedStatement.setString(2,email);
                preparedStatement.setInt(3,UserStatus.DELETED.getCode());
                rowUpdates = preparedStatement.executeUpdate();
                if(rowUpdates>0){
                    log.info("================== User Activated ====================");
                }
            }
            connection.commit();
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new ResponseDto(Constants.errorMessage,null,false);
        }
        return new ResponseDto(Constants.successMessage,null,true);
    }

}
