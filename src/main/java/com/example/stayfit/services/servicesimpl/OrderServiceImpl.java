package com.example.stayfit.services.servicesimpl;

import com.example.stayfit.aws.EmailHandler;
import com.example.stayfit.dbconfig.PostgresQlConfig;
import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.Product;
import com.example.stayfit.dtos.ResponseDto;
import com.example.stayfit.services.OrderService;
import com.example.stayfit.utility.Constants;
import com.example.stayfit.utility.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    PostgresQlConfig postgresQlConfig;
    EmailHandler emailHandler;
    @Autowired
   public OrderServiceImpl(PostgresQlConfig postgresQlConfig,EmailHandler emailHandler){
        this.postgresQlConfig = postgresQlConfig;
        this.emailHandler = emailHandler;
   }


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

            statement = connection.createStatement();
            String insertIntoOrdersQuery = QueryUtil.getInsertOrderQuery(order);
            resultSet = statement.executeQuery(insertIntoOrdersQuery);

            if(resultSet.next()){
                Long orderId = resultSet.getLong(1);
                log.info("================ Order created successfully ===================");
                String insertIntoOrderDetailsQuery = QueryUtil.getInsertOrderDetailsQuery(order.getProducts(),orderId);
                statement.executeUpdate(insertIntoOrderDetailsQuery);
                log.info("=============== Order details inserted =====================");
                if(order.getPaymentMethod().equalsIgnoreCase("1")) {
                    String insertIntoOrderBillingQuery = QueryUtil.getInsertOrderBillingQuery(order.getPaymentDto(), orderId);
                    statement.executeUpdate(insertIntoOrderBillingQuery);
                }
                for(Product product: order.getProducts()){
                    String updateProductStockQuery = QueryUtil.getUpdateProductStockQuery(product);
                    statement.executeUpdate(updateProductStockQuery);
                }
                connection.commit();

                String htmlBody = null;
                if(order.getPaymentMethod().equalsIgnoreCase("1")) {
                  htmlBody = String.format("""
                                    <html>
                                        <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                                            <h2 style="color: #2e6c80;">Order Placed Successfully</h2>
                                            
                                            <p>Dear Customer,</p>
                                            
                                            <p>Thank you for your order! Here are the details of your transaction:</p>
                                            
                                            <table style="border-collapse: collapse; width: 100%%; margin-top: 10px;">
                                                <tr>
                                                    <td style="padding: 8px; border: 1px solid #ccc;"><strong>Order ID</strong></td>
                                                    <td style="padding: 8px; border: 1px solid #ccc;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 8px; border: 1px solid #ccc;"><strong>Payment ID</strong></td>
                                                    <td style="padding: 8px; border: 1px solid #ccc;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 8px; border: 1px solid #ccc;"><strong>Payment Time</strong></td>
                                                    <td style="padding: 8px; border: 1px solid #ccc;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 8px; border: 1px solid #ccc;"><strong>Amount Paid</strong></td>
                                                    <td style="padding: 8px; border: 1px solid #ccc;">%s</td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin-top: 20px;">If you have any questions, feel free to reach out to our support team.</p>
                                            
                                            <p>Best regards,<br/>
                                            The Team</p>
                                        </body>
                                    </html>
                                    """,
                            orderId,
                            order.getPaymentDto().getPaymentIntentId(),
                            new Timestamp(Long.valueOf(order.getPaymentDto().getCreated()) * 1000),
                            order.getPaymentDto().getAmount()
                    );
                }
                else{

                }

                emailHandler.sendEmail(order.getEmail(),"Order placed successfully",htmlBody);
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
