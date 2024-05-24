package com.khaphp.orderservice.dto.OrderDetail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDetailDTOcreate {
    private String foodId;
    private float amount;
    private float price;
}
