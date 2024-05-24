package com.khaphp.orderservice.dto.Order;

import com.khaphp.common.dto.usersystem.UserSystemDTOviewInOrtherEntity;
import com.khaphp.orderservice.dto.OrderDetail.OrderDetailDToviewInOrtherEntity;
import com.khaphp.orderservice.entity.PaymentOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDTOviewDetail {
    private String id;
    private Date createDate;
    private Date updateDate;
    private Date deliveryTime;
    private String status;
    private float totalPrice;
    private UserSystemDTOviewInOrtherEntity employeeV;
    private UserSystemDTOviewInOrtherEntity shipperV;
    private UserSystemDTOviewInOrtherEntity customerV;
    private List<OrderDetailDToviewInOrtherEntity> orderDetailsV;
    private PaymentOrder paymentOrder;
}
