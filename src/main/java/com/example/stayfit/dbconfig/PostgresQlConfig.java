package com.example.stayfit.dbconfig;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.utility.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class PostgresQlConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private EmailHandler emailHandler;

    @Autowired
    PostgresQlConfig(EmailHandler emailHandler){
        this.emailHandler = emailHandler;
    }

    public Connection getConnection(){
      log.info("=================== connecting ==================");
      Connection connection = null;
      try {
           connection = DriverManager.getConnection(url, username, password);
      }catch (Exception ex){
          log.info("=================== connection failed ==================");
      }
        log.info("=================== connected ==================");
      return connection;
    }

    public void closeResources(Connection connection, Statement statement, PreparedStatement preparedStatement, ResultSet resultSet){
        try{
            if(resultSet!=null)
                resultSet.close();
            if(preparedStatement!=null)
                preparedStatement.close();
            if(statement!=null)
                statement.close();
            if(connection!=null)
                connection.close();
        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject, ex.getMessage());
        }
    }
}
