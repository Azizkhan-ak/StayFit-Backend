package com.example.stayfit.dbconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
@Component
public class PostgresQlConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

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
}
