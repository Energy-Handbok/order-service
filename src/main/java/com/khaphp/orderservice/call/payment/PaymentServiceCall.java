package com.khaphp.orderservice.call.payment;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.common.dto.payment.WalletDTOupdate;
import com.khaphp.common.dto.payment.WalletTransactionDTOcreate;
import com.khaphp.common.entity.Wallet;
import com.khaphp.orderservice.call.notiservice.NotiServiceCall;
import com.khaphp.orderservice.call.notiservice.NotiServiceFeignClient;
import com.khaphp.orderservice.constant.StatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceCall {
    public static final String CALL_OTHER_SERVICE_ERROR = "Call other service error: {}";
    public static final String CALL_OTHER_SERVICE = "Call other service";
    public static final String RESPONSE_MSG = "response ";
    private final PaymentServiceFeignClient paymentServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Wallet detailObject(String customerId){
        String where = "[ " + PaymentServiceCall.log.getName() + " detail]";
        return (Wallet) circuitBreakerFactory.create("detailWallet").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = paymentServiceFeignClient.detailObject(customerId); //data: Notification
                    log.info(RESPONSE_MSG + where + ": " + responseEntity);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        return getObjectFromResponse((LinkedHashMap<String, Object>) responseEntity.getBody());
                    } else {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        if(responseObject != null){
                            throw new RuntimeException(responseObject.getMessage());
                        }
                        return null;
                    }
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return null;
                });
    }

    public String updateObjectBalance(WalletDTOupdate object){
        String where = "[ " + PaymentServiceCall.log.getName() + " update]";
        return (String) circuitBreakerFactory.create("updateWallet").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = paymentServiceFeignClient.updateObjectBalance(object); //data: Notification
                    log.info(RESPONSE_MSG + where + ": " + responseEntity);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        return StatusResponse.SUCCESS.toString();
                    } else {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        return Objects.requireNonNull(responseObject).getMessage();
                    }
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return StatusResponse.FAILURE.toString();
                });
    }

    public String createObject(WalletTransactionDTOcreate object) {
        String where = "[ " + PaymentServiceCall.log.getName() + " create transaction]";
        return (String) circuitBreakerFactory.create("createTransaction").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = paymentServiceFeignClient.createObject(object); //data: Notification
                    log.info(RESPONSE_MSG + where + ": " + responseEntity);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) responseEntity.getBody();
                        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) body.get("data");
                        return StatusResponse.SUCCESS.toString() + "|" + data.get("id");
                    } else {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        return Objects.requireNonNull(responseObject).getMessage();
                    }
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return StatusResponse.FAILURE.toString();
                });
    }

    private Wallet getObjectFromResponse(LinkedHashMap<String, Object> reponse) {
        return Wallet.getObjectFromLinkedHashMap((LinkedHashMap<String, Object>) reponse.get("data"));
    }
}
