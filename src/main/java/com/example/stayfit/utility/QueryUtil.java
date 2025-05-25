package com.example.stayfit.utility;

public class QueryUtil {

    public static String getAllProductsQuery(Integer category){
        StringBuilder query = new StringBuilder("select ID,UPPER(replace(NAME,'_',' ')) ,description ,status ,category ,price ,discount_in_percent ,items_in_stock ,s3_url from public.products p where status !=0 ");
        if(category!=null && category.intValue()!=0){
            query.append(" and category = ").append(category);
        }
        return query.toString();
    }
}
