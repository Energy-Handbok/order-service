package com.khaphp.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Food {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    private String name;
    private String unit;
    private double stock;
    private double price;
    private int sale; //from 1 to 100
    private String img;
    private String location;
    private Date updateDate;
    private String status;
    @Column(columnDefinition = "VARCHAR(36)")
    private String employeeId;
    @ManyToOne
    @JsonIgnore
    private FoodEncylopedia foodEncylopedia;

    public static Food mapFromLinkedHashMap(LinkedHashMap<String, Object> linkedHashMap) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Food food = new Food();
        food.setId((String) linkedHashMap.get("id"));
        food.setName((String) linkedHashMap.get("name"));
        food.setUnit((String) linkedHashMap.get("unit"));
        food.setStock((double) linkedHashMap.get("stock"));
        food.setPrice((double) linkedHashMap.get("price"));
        food.setSale((int) linkedHashMap.get("sale"));
        food.setImg((String) linkedHashMap.get("img"));
        food.setLocation((String) linkedHashMap.get("location"));
        food.setUpdateDate(simpleDateFormat.parse((String) linkedHashMap.get("updateDate")));
        food.setStatus((String) linkedHashMap.get("status"));
        food.setEmployeeId((String) linkedHashMap.get("employeeId"));
        return food;
    }
}
