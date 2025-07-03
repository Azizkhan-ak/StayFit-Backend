package com.example.stayfit.security;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import com.example.stayfit.utility.UserRole;
import com.example.stayfit.utility.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    PostgresQlConfig postgresQlConfig;
    EmailHandler emailHandler;

    @Autowired
    public UserDetailsServiceImpl(PostgresQlConfig postgresQlConfig,EmailHandler emailHandler){
        this.emailHandler = emailHandler;
        this.postgresQlConfig = postgresQlConfig;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = postgresQlConfig.getConnection();
            String queryToUserByEmail = QueryUtil.getUserByEmailQuery();
            statement = connection.prepareStatement(queryToUserByEmail);
            statement.setString(1,username);
            statement.setInt(2, UserStatus.ACTIVE.getCode());
            resultSet = statement.executeQuery();

            if(resultSet.next()){
                String userName = resultSet.getString(1);
                String password = resultSet.getString(2);
                GrantedAuthority role = new SimpleGrantedAuthority(Integer.valueOf(resultSet.getInt(3)).toString());
                return new User(userName, password, Collections.singleton(role));
            }
            else {
                throw new UsernameNotFoundException("User not found with email: " + username);
            }

        }catch (Exception ex){
            ex.printStackTrace();
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        finally {
            try{
                if(resultSet!=null)
                    resultSet.close();
                if(statement!=null)
                    statement.close();
                if(resultSet!=null)
                    resultSet.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
