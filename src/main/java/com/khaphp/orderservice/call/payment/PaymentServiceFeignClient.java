package com.khaphp.orderservice.call.payment;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.khaphp.common.dto.payment.*;

@FeignClient(name = "payment-service")
public interface PaymentServiceFeignClient {
    @GetMapping("/api/v1/wallet/detail")
    public ResponseEntity<Object> detailObject(@RequestParam String customerId);
    @PutMapping("/api/v1/wallet/customer-balance")
    public ResponseEntity<Object> updateObjectBalance(@RequestBody @Valid WalletDTOupdate object);

    @PostMapping("/api/v1/wallet-transaction")
    public ResponseEntity<Object> createObject(@RequestBody @Valid WalletTransactionDTOcreate object);
}
