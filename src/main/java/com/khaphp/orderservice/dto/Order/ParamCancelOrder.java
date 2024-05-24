package com.khaphp.orderservice.dto.Order;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ParamCancelOrder {
    private String orderId;
    private String userId;
    private String role;
}
