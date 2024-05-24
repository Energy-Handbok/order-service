package com.khaphp.orderservice.dto.Order;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ParamOrderShipperId {
    private String orderId;
    private String shipperId;
}
