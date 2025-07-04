package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;
import com.example.stayfit.security.JwtUtil;
import com.example.stayfit.services.AuthenticationService;
import com.example.stayfit.utility.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private PostgresQlConfig postgresQlConfig;
    private AuthenticationManager authManager;
    private EmailHandler emailHandler;

    @Autowired
    AuthenticationServiceImpl(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, PostgresQlConfig postgresQlConfig, AuthenticationManager authenticationManager, EmailHandler emailHandler) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.postgresQlConfig = postgresQlConfig;
        this.authManager = authenticationManager;
        this.emailHandler = emailHandler;
    }


    @Override
    public ResponseDto login(UserDto userDto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            String token = jwtUtil.generateToken(user.getUsername(),authorities.iterator().next().getAuthority());
            return new ResponseDto(Constants.successMessage,token,true);
        } catch (BadCredentialsException e) {
            return new ResponseDto("Invalid Credentials",null,false);
        }
    }

    @Override
    public ResponseDto register(UserDto userDto) {
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
            String username = userDto.getFirstName();
            String verificationLink = String.format(URLs.verfiyEmailUrl,token,userDto.getEmail());
            String htmlBody = String.format(htmlTemplate, username, verificationLink, verificationLink, verificationLink);
            String insertIntoEmailVerificationTokens = QueryUtil.getInsertIntoEmailVerificationQuery(userDto.getEmail(),token );

            connection = postgresQlConfig.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(QueryUtil.getCheckIfEmailAlreadyExists(userDto.getEmail()));
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                if(resultSet.getInt(3)== UserStatus.INACTIVE.getCode()){
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
                PreparedStatement ps = connection.prepareStatement(QueryUtil.getUserInsertQuery());
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, UserStatus.INACTIVE.getCode());
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, UserRole.CUSTOMER.getCode()); // 0 role for customer
                ps.setString(5, userDto.getFirstName());
                ps.setString(6, userDto.getLastName());
                ps.setString(7, userDto.getEmail());
                ps.setString(8, passwordEncoder.encode(userDto.getPassword()));
                ps.setString(9, userDto.getCity());
                ps.setString(10, userDto.getCountry());
                ps.setString(11, userDto.getAddress());
                ps.setString(12, userDto.getPhone());
                ps.setInt(13,AuthProvider.LOCAL_AUTH.getCode());
                ps.executeUpdate();

                preparedStatement = connection.prepareStatement(insertIntoEmailVerificationTokens);
                preparedStatement.executeUpdate();

                emailHandler.sendEmail(userDto.getEmail(),Constants.verifyEmailSubject,htmlBody);

                connection.commit();
                return new ResponseDto(Constants.verifyEmailMessage,null,true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                emailHandler.sendErrorEmail(Constants.exceptionSubject, e.getMessage());
                throw new RuntimeException(e);
            }
            return new ResponseDto(Constants.errorMessage,null,false);
        } finally {
           postgresQlConfig.closeResources(connection,preparedStatement,null,resultSet);
        }
    }

    @Override
    public ResponseDto verifyEmail(String oneTimeToken, String email) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try{
            connection = postgresQlConfig.getConnection();

            connection.setAutoCommit(false);
            String updateTokenStatus = QueryUtil.getVerifyEmailQuery();
            preparedStatement = connection.prepareStatement(updateTokenStatus);
            preparedStatement.setString(1,oneTimeToken);
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
        finally {
            postgresQlConfig.closeResources(connection,preparedStatement,null,null);
        }
        return new ResponseDto(Constants.successMessage,null,true);
    }

    public ResponseDto passwordReset(String token,String password){

        ResponseDto responseDto = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try{
            connection = postgresQlConfig.getConnection();

            if(connection == null){
                emailHandler.sendErrorEmail(Constants.databaseConnectionError,Constants.databaseConnectionError);
                return new ResponseDto(Constants.errorMessage,null,false);
            }

            preparedStatement = connection.prepareStatement(QueryUtil.getUserAssociatedWithTokenQuery());
            preparedStatement.setString(1,token);
            resultSet = preparedStatement.executeQuery();
            String email = "";

            if(resultSet.next()){
                email = resultSet.getString(1);
                String passwordResetQuery = QueryUtil.getUserPasswordResetQuery();
                preparedStatement = connection.prepareStatement(passwordResetQuery);

                preparedStatement.setString(1,passwordEncoder.encode(password));
                preparedStatement.setString(2,email);
                preparedStatement.setInt(3,UserStatus.DELETED.getCode());
                int result = preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement(QueryUtil.getMakePasswordResetTokenAsUsed());
                preparedStatement.setBoolean(1,true);
                preparedStatement.setString(2,token);
                preparedStatement.executeUpdate();

                if(result != 1){
                    emailHandler.sendErrorEmail(Constants.errorMessage,Constants.errorMessage);
                    return new ResponseDto(Constants.errorMessage,null,false);
                }

                responseDto = new ResponseDto(Constants.successMessage,null,true);
            }
            else{
                return new ResponseDto(Constants.tokenExpired,null,false);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            postgresQlConfig.closeResources(connection,preparedStatement,null,resultSet);
        }
        return responseDto;
    }

    public ResponseDto sendPasswordResetEmail(String email){

        ResponseDto responseDto = null;
        Connection connection = null;
        PreparedStatement statement = null;

        try{

            connection = postgresQlConfig.getConnection();
            if(connection == null){
                emailHandler.sendErrorEmail(Constants.databaseConnectionError,Constants.unableToObtainConnection);
            }

            String uuid = UUID.randomUUID().toString();
            String insertQuery = QueryUtil.getInsertOneTimeTokenQueryForPasswordReset();

            statement = connection.prepareStatement(insertQuery);
            statement.setString(1,uuid);
            statement.setString(2,email);
            int result = statement.executeUpdate();

            if(result != 1){
                emailHandler.sendErrorEmail(Constants.errorMessage,Constants.errorMessage);
                return new ResponseDto(Constants.errorMessage,null,false);
            }

            String resetLink = "http://localhost:5173/resetPassword?token=" + uuid;

            String emailStr = "<p>Dear User,</p>"
                    + "<p>We received a request to reset your password. Click the link below to reset it:</p>"
                    + "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
                    + "<p>If you did not request this, you can safely ignore this email.</p>"
                    + "<p>Regards,<br>StayFit Support Team</p>";

            emailHandler.sendEmail(email,"Password Reset",emailStr);
            responseDto = new ResponseDto(Constants.successMessage,null,true);
        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
        finally {
            postgresQlConfig.closeResources(connection,null,statement,null);
        }
        return responseDto;
    }

    public ResponseDto registerOAuth(String token){

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList("809742363195-7med73ik08h4cmsee308a1io8abtfur4.apps.googleusercontent.com")) // Your frontend client_id
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                connection = postgresQlConfig.getConnection();

                String email = payload.getEmail();
                String firstName = (String) payload.get("given_name");
                String lastName = (String) payload.get("family_name");
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified().toString());
                if(emailVerified){
                    // check if user with this email already exists, we dont do anything , just return jwt.
                    //else we create a new user with email, with random uuid as password
                    String queryToUserByEmail = QueryUtil.getUserByEmailQuery();
                    statement = connection.prepareStatement(queryToUserByEmail);
                    statement.setString(1,email);
                    statement.setInt(2, UserStatus.ACTIVE.getCode());
                    resultSet = statement.executeQuery();

                    if(resultSet.next()){
                        Integer role = resultSet.getInt(3);
                        String userGeneratedToken = jwtUtil.generateToken(email,role.toString());
                        return new ResponseDto(Constants.successMessage,userGeneratedToken,true);
                    }
                    else{
                        statement = connection.prepareStatement(QueryUtil.getUserInsertQuery());
                        statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                        statement.setInt(2, UserStatus.ACTIVE.getCode());
                        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                        statement.setInt(4, UserRole.CUSTOMER.getCode()); // 0 role for customer
                        statement.setString(5, firstName);
                        statement.setString(6, lastName);
                        statement.setString(7, email);
                        statement.setString(8, passwordEncoder.encode(UUID.randomUUID().toString()));
                        statement.setString(9, "unknown");
                        statement.setString(10, "unknown");
                        statement.setString(11, "unknown");
                        statement.setString(12, "unknown");
                        statement.setInt(13,AuthProvider.GOOGLE_AUTH.getCode());
                        statement.executeUpdate();

                        Integer role = UserRole.CUSTOMER.getCode();
                        String userGeneratedToken = jwtUtil.generateToken(email,role.toString());
                        return new ResponseDto(Constants.successMessage,userGeneratedToken,true);
                    }
                }
                else{
                    return new ResponseDto(Constants.errorMessage,null,false);
                }
            } else {
                return new ResponseDto(Constants.errorMessage,null,false);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return new ResponseDto(Constants.errorMessage,null,false);
        }
        finally {
           postgresQlConfig.closeResources(connection,null,statement,resultSet);
        }
    }
}
