package com.khaphp.orderservice.service.kafka;

import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.dto.payment.WalletDTOupdate;
import com.khaphp.orderservice.constant.TopicEventKafka;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaMessagePulisher {
    private final KafkaTemplate<String, Object> template;

    public void deleteTransactionById(String idTransaction){
        CompletableFuture<SendResult<String, Object>> future = template.send(TopicEventKafka.DELETE_TRANSACTION.name(), idTransaction);
        future.whenComplete((result, ex) -> {
            if(ex == null){
               System.out.println("Send message=[" + idTransaction + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }else{
               System.out.println("Unable to send message=[" + idTransaction + "] due to : " + ex.getMessage());
            }
        });
    }

    public void updateBalanceWallet(WalletDTOupdate walletDTOupdate){
        CompletableFuture<SendResult<String, Object>> future = template.send(TopicEventKafka.UPDATE_WALLET_BALANCE.name(), walletDTOupdate);
        future.whenComplete((result, ex) -> {
            if(ex == null){
                System.out.println("Send message=[" + walletDTOupdate.toString() + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }else{
                System.out.println("Unable to send message=[" + walletDTOupdate.toString() + "] due to : " + ex.getMessage());
            }
        });
    }

    public void updateStockFood(FoodDTOupdate object){
        CompletableFuture<SendResult<String, Object>> future = template.send(TopicEventKafka.UPDATE_STOCK_FOOD.name(), object);
        future.whenComplete((result, ex) -> {
            if(ex == null){
                System.out.println("Send message=[" + object.toString() + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }else{
                System.out.println("Unable to send message=[" + object.toString() + "] due to : " + ex.getMessage());
            }
        });
    }

//    public void sendEventsToTopic(Customer customer){
//        try{
//            CompletableFuture<SendResult<String, Object>> future = template.send(topicCustomer, customer);
//            future.whenComplete((result, ex) -> {
//                if(ex == null){
//                    System.out.println("Send message=[" + customer.toString() + "] with offset=[" + result.getRecordMetadata().offset() + "]");
//                }else{
//                    System.out.println("Unable to send message=[" + customer.toString() + "] due to : " + ex.getMessage());
//                }
//            });
//        }catch (Exception ex){
//            System.out.println("ERROR: " + ex.getMessage());
//        }
//    }
}
