package com.khaphp.orderservice.service.vnpay;

import com.khaphp.common.dto.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    ResponseObject<Object> createPayment(HttpServletRequest req, int amount_param, String customerId, boolean isThirdParty, String orderId);
    ResponseObject<Object> resultTransaction(String vnp_Amount, String vnp_BankCode, String vnp_OrderInfo, String vnp_PayDate, String vnp_ResponseCode);
}
