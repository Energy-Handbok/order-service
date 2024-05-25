package com.khaphp.orderservice.controller;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.orderservice.service.vnpay.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Object> getAll(HttpServletRequest req,
                                    @RequestParam @Min(10000) int amount,
                                    @RequestParam String customerId){
        ResponseObject<Object> responseObject = paymentService.createPayment(req, amount, customerId, false, "");
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @GetMapping("/payment_result")
    public ResponseEntity<Object> transaction(   //hàm bắt kq giao dịch (transaction - success, fail, hủy thanh toán, ...) về từ VNPAY
                                            @RequestParam(value = "vnp_Amount") String vnp_Amount,
                                            @RequestParam(value = "vnp_BankCode") String vnp_BankCode,
                                            @RequestParam(value = "vnp_OrderInfo") String vnp_OrderInfo,
                                            @RequestParam(value = "vnp_PayDate") String vnp_PayDate,
                                            @RequestParam(value = "vnp_ResponseCode") String vnp_ResponseCode) {
        ResponseObject<Object> rs = paymentService.resultTransaction(vnp_Amount, vnp_BankCode, vnp_OrderInfo, vnp_PayDate, vnp_ResponseCode);
        if(rs.getCode() == 200){
            return ResponseEntity.ok(rs);
        }else {
            return ResponseEntity.badRequest().body(rs);
        }
//      HttpHeaders headers = new HttpHeaders();
//        if (rs.getCode() == 200) {
//            rs.setCode(200);
//            if(rs.getMessage().contains("guest")){
//                headers.add(HttpHeaders.LOCATION, vnPayHelper.vnp_RedirectResultGuestBooking_success);
//            }else{
//                headers.add(HttpHeaders.LOCATION, VnPayHelper.vnp_RedirectResult + "?status=success");
//            }
            //redirect qua URl khác
//            return new ResponseEntity<>(headers, HttpStatus.FOUND);
//        }else{
//            rs.setCode(400);
//            if(rs.getMessage().contains("guest")){
//                headers.add(HttpHeaders.LOCATION, VnPayHelper.vnp_RedirectResultGuestBooking_fail);
//            }else{
//                headers.add(HttpHeaders.LOCATION, VnPayHelper.vnp_RedirectResult + "?status=fail");
//            }
//            return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
