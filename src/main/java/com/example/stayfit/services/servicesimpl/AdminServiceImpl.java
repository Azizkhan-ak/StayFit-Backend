package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.Product;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.AdminService;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private PostgresQlConfig postgresQlConfig;
    private EmailHandler emailHandler;

    @Autowired
    AdminServiceImpl(PostgresQlConfig postgresQlConfig,EmailHandler emailHandler){
        this.postgresQlConfig = postgresQlConfig;
        this.emailHandler = emailHandler;
    }

    @Override
    public ResponseDto listInventory(String token) {
        ResponseDto responseDto = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            List<Product> productList = new ArrayList<>();
            connection = postgresQlConfig.getConnection();

            if(connection == null){
                emailHandler.sendErrorEmail(Constants.exceptionSubject,Constants.unableToObtainConnection);
            }

            statement = connection.createStatement();
            String inventoryQuery = QueryUtil.getAllProductsQuery(null);
            resultSet = statement.executeQuery(inventoryQuery);
            while (resultSet.next()){
                Product product = new Product();
                product.setId(resultSet.getInt(1));
                product.setName(resultSet.getString(2));
                product.setDesc(resultSet.getString(3));
                product.setStatus(resultSet.getInt(4));
                product.setCategory(resultSet.getInt(5));
                product.setPrice(resultSet.getFloat(6));
                product.setDiscountInPercent(resultSet.getFloat(7));
                product.setItemsInStock(resultSet.getInt(8));
                product.setImgUrl(resultSet.getString(9));
                productList.add(product);
            }
            responseDto = new ResponseDto(Constants.successMessage,productList,true);

        }catch (Exception ex){
            ex.printStackTrace();
            emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            return new ResponseDto(Constants.errorMessage,null,false);
        }
        finally {
            try {
                if(resultSet!=null) resultSet.close();
                if(statement!=null) statement.close();
                if(connection!=null) connection.close();
            }catch (Exception ex){
                ex.printStackTrace();
                emailHandler.sendErrorEmail(Constants.exceptionSubject,ex.getMessage());
            }
        }
        return responseDto;
    }
}
