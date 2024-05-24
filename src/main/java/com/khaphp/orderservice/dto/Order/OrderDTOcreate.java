package com.khaphp.orderservice.dto.Order;

import com.khaphp.orderservice.dto.OrderDetail.OrderDetailDTOcreate;
import com.khaphp.orderservice.util.valid.MethodOrder.ValidMethod;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDTOcreate {
    private String customerId;
    private List<OrderDetailDTOcreate> orderDetails;
    @ValidMethod
    private String method;
    //--guest payment
    private String phoneGuest;
    private String nameGuest;
    private String address;
}
