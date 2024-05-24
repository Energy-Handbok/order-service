package com.khaphp.orderservice.service;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.orderservice.dto.Order.OrderDTOcreate;
import com.khaphp.orderservice.dto.Order.OrderDTOupdate;
import jakarta.servlet.http.HttpServletRequest;

public interface OrdersService {

    ResponseObject<Object> getAll(int pageSize, int pageIndex);
    ResponseObject<Object> getDetail(String id);
    ResponseObject<Object> create(OrderDTOcreate object) throws Exception;
    ResponseObject<Object> orderThirdParty(HttpServletRequest req, OrderDTOcreate object) throws Exception;
    ResponseObject<Object> updateStatus(OrderDTOupdate object);
    ResponseObject<Object> updateShipperTakeOrder(String orderId, String shipperId);
    ResponseObject<Object> finishDeilivery(String orderId, String shipperId);
    ResponseObject<Object> cancelOrder(String orderId, String customerId, String role);
    ResponseObject<Object> delete(String id);
}
