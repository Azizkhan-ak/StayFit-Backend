package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.services.ProductsService;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.Product;
import com.example.stayfit.dtos.ResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductsServiceImpl implements ProductsService {

    PostgresQlConfig postgresQlConfig;

    @Autowired
    public ProductsServiceImpl(PostgresQlConfig postgresQlConfig){
        this.postgresQlConfig = postgresQlConfig;
    }

    public ResponseDto getAllProducts(Integer category){
        ResponseDto responseDto = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try{
            connection = postgresQlConfig.getConnection();
            if(connection!=null) {
                List<Product> productList = new ArrayList<>();
                String queryToGetAllProducts = QueryUtil.getAllProductsQuery(category);
                statement = connection.createStatement();
                resultSet = statement.executeQuery(queryToGetAllProducts);
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
            }
            else{
                return new ResponseDto(Constants.errorMessage,null,false);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return new ResponseDto(Constants.errorMessage,null,false);
        }
        finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if(statement!=null)
                    statement.close();
                if(connection!=null)
                    connection.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return responseDto;
    }
}
