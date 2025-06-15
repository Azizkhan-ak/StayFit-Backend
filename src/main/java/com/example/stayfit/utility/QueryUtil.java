package com.example.stayfit.utility;

import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.PaymentDto;
import com.example.stayfit.dtos.Product;
import com.example.stayfit.dtos.UserDto;
import org.apache.catalina.User;

import java.sql.Timestamp;
import java.util.List;

public class QueryUtil {

    public static String getAllProductsQuery(Integer category){
        StringBuilder query = new StringBuilder("select ID,UPPER(replace(NAME,'_',' ')) ,description ,status ,category ,price ,discount_in_percent ,items_in_stock ,s3_url from public.products p where status !=0 ");
        if(category!=null && category.intValue()!=0){
            query.append(" and category = ").append(category);
        }
        return query.toString();
    }

    public static String getInsertOrderQuery(Order order){
        return "INSERT INTO public.orders\n" +
                "(id,created_at, status, modified_at, user_id, city, country, delivery_address, phone, payment_method, use_user_contact, \"name\",email)\n" +
                "VALUES(nextval('orders_id_seq'),CURRENT_TIMESTAMP, 1, null, 0, '"+order.getCity()+"', '"+order.getCountry()+"', '"+order.getDeliveryAddress()+"', '"+order.getPhone()+"', "+(order.getPaymentMethod())+", false, '"+order.getName()+"','"+order.getEmail()+"') " +
                "RETURNING ID";
    }

    public static String getInsertOrderDetailsQuery(List<Product> productList,Long orderId){
        StringBuilder insertQuery = new StringBuilder("INSERT INTO public.order_details (order_id,product_id,quantity)" +
                "VALUES  ");
        for(Product product: productList){
            insertQuery.append("(").append(orderId).append(",").append(product.getId()).append(",").append(product.getQuantity()).append("),");
        }
        insertQuery = insertQuery.deleteCharAt(insertQuery.length()-1);
        return insertQuery.toString();
    }

    public static String getInsertOrderBillingQuery(PaymentDto payementDto,Long orderId){
        String insertQuery  = "INSERT INTO public.order_billing (bill_payed_at,amount_payed,currency,order_id,payment_intent_id)" +
                "values ('"+new Timestamp(Long.valueOf(payementDto.getCreated())*1000)+"',"+payementDto.getAmount()+",'"+payementDto.getCurrency()+"',"+orderId+",'"+payementDto.getPaymentIntentId()+"') ";
        return insertQuery;
    }

    public static String getUpdateProductStockQuery(Product product){
        return "UPDATE public.products set items_in_stock = items_in_stock - "+product.getQuantity()+" where id = "+product.getId()+" and status = 1 ";
    }

    public static String getCheckIfEmailAlreadyExists(String email){
        return "SELECT id,email,status,first_name from public.users where email = '"+email+"' and status !=6 ";
    }

    public static String getUserByEmailQuery(String email){
        return "SELECT EMAIL,PASSWORD,ROLE FROM public.users where email = '"+email.trim().replaceAll("'","''")+"' and status = "+UserStatus.ACTIVE.getCode();
    }

    public static String getInsertIntoEmailVerificationQuery(String email,String token){
        return "INSERT into public.email_verification_tokens (email,token) values ('"+email+"','"+token+"')";
    }

    public static String getVerifyEmailQuery(){
        return "update public.email_verification_tokens \n" +
                "set is_used = true , verified_at  = current_timestamp \n" +
                "where \"token\"  = ?\n" +
                "and expires_at > current_timestamp ";
    }

    public static String getActivateUserQuery(){
        return "update public.users set status = ? where email = ? and status != ?";
    }

}
