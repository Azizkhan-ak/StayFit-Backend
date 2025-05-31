package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.dtos.UserDto;
import com.example.stayfit.services.UserService;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
public class UserServiceImpl implements UserService {

    EmailHandler emailHandler;
    PostgresQlConfig postgresQlConfig;

    @Autowired
    UserServiceImpl(EmailHandler emailHandler,PostgresQlConfig postgresQlConfig){
        this.emailHandler = emailHandler;
        this.postgresQlConfig = postgresQlConfig;
    }

    @Override
    public ResponseDto signUp(UserDto userDto) {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try{
            connection = postgresQlConfig.getConnection();
            if(connection == null){
                emailHandler.sendErrorEmail(Constants.exceptionSubject,Constants.unableToObtainConnection);
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery(QueryUtil.getCheckIfEmailAlreadyExists(userDto.getEmail()));
            if(resultSet.next()){
                return new ResponseDto(Constants.userAlreadyExists,null,false);
            }
            else{

            }
        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
        }
        return null;
    }

    @Override
    public ResponseDto signIn(UserDto userDto) {
        return null;
    }
}
