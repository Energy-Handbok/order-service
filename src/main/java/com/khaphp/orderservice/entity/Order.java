package com.khaphp.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    private Date createDate;
    private Date updateDate;
    private Date deliveryTime;
    private String status;
    private float totalPrice;

    @Column(columnDefinition = "VARCHAR(36)")
    private String employeeId;

    @Column(columnDefinition = "VARCHAR(36)")
    private String shipperId;

    @Column(columnDefinition = "VARCHAR(36)")
    private String customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private PaymentOrder paymentOrder;
}
