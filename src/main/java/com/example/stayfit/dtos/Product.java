package com.example.stayfit.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Product {
    private Integer id;
    private String name;
    private String desc;
    private Integer status;
    private Integer category;
    private Float price;
    private Float discountInPercent;
    private Integer itemsInStock;
    private String imgUrl;
    private Integer quantity;
}
