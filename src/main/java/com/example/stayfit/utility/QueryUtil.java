package com.example.stayfit.utility;

import com.example.stayfit.dtos.Order;
import com.example.stayfit.dtos.PayementDto;
import com.example.stayfit.dtos.Product;

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
            insertQuery.append("(").append(orderId).append(product.getId()).append(product.getQuantity()).append("),");
        }
        insertQuery = insertQuery.deleteCharAt(insertQuery.length()-1);
        return insertQuery.toString();
    }

    public static String getInsertOrderBillingQuery(PayementDto payementDto,Long orderId){
        String insertQuery  = "INSERT INTO public.order_billing (bill_payed_at,amount_payed,currency,order_id,payment_intent_id)" +
                "values ('"+Timestamp.valueOf(payementDto.getCreated())+"',"+payementDto.getAmount()+",'"+payementDto.getCurrency()+"',"+orderId+",'"+payementDto.getPayementIntentId()+"') ";
        return insertQuery;
    }
}
