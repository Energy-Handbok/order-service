package com.khaphp.orderservice.controller;


import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.dto.payment.WalletDTOupdate;
import com.khaphp.orderservice.service.kafka.KafkaMessagePulisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-send-msg-to-kafka")
@RequiredArgsConstructor
public class TestSendMSGToKafka {
    private final KafkaMessagePulisher kafkaMessagePulisher;

    @GetMapping
    public String testSend(){
        kafkaMessagePulisher.updateBalanceWallet(WalletDTOupdate.builder().balance(10000).customerId("1").build());
        return "success";
    }

    @PostMapping
    public String testSend2(){
        kafkaMessagePulisher.updateStockFood(FoodDTOupdate.builder().id("111").stock(9).build());
        return "success";
    }

    @DeleteMapping
    public String testSend3(){
        kafkaMessagePulisher.deleteTransactionById("1111111");
        return "success";
    }
}
