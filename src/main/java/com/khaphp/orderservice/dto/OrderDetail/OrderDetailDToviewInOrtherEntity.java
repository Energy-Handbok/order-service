package com.khaphp.orderservice.dto.OrderDetail;

import com.khaphp.common.dto.food.FoodDTOviewInOrtherEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDetailDToviewInOrtherEntity {
    private FoodDTOviewInOrtherEntity foodV;
    private float amount;
    private float price;
}
