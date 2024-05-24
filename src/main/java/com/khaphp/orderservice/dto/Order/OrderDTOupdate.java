package com.khaphp.orderservice.dto.Order;

import com.khaphp.orderservice.util.valid.StatusOrder.ValidStatusOrder;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDTOupdate {
    private String orderId;
    private String employeeId;
    @ValidStatusOrder
    private String status;
}
