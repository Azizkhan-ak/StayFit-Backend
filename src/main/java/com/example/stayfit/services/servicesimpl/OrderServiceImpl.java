package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.OrderService;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    PostgresQlConfig postgresQlConfig;
    @Override
    public ResponseDto placeOrder(Order order) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = postgresQlConfig.getConnection();
            connection.setAutoCommit(false);

            if(connection == null){
                return new ResponseDto(Constants.errorMessage,null,false);
            }

            String insertIntoOrdersQuery = QueryUtil.getInsertOrderQuery(order);
            resultSet = statement.executeQuery(insertIntoOrdersQuery);

            if(resultSet.next()){
                Long orderId = resultSet.getLong(1);
                log.info("================ Order created successfully ===================");
                String insertIntoOrderDetailsQuery = QueryUtil.getInsertOrderDetailsQuery(order.getProducts(),orderId);
                statement.executeUpdate(insertIntoOrderDetailsQuery);
                log.info("=============== Order details inserted =====================");
                String insertIntoOrderBillingQuery = QueryUtil.getInsertOrderBillingQuery(order.getPayementDto(),orderId);
                statement.executeUpdate(insertIntoOrderBillingQuery);
                connection.commit();
            }

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
            try{
                if(resultSet!=null)
                    resultSet.close();
                if(statement!=null)
                    statement.close();
                if(connection!=null)
                    connection.commit();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return new ResponseDto("Request has been processed",null,true);
    }
}
