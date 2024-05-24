package com.khaphp.orderservice.call.notiservice;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.common.dto.noti.NotificationDTOcreate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotiServiceCall {
    public static final String CALL_OTHER_SERVICE_ERROR = "Call other service error: {}";
    public static final String CALL_OTHER_SERVICE = "Call other service";
    private final NotiServiceFeignClient notiServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public boolean create(NotificationDTOcreate object) {
        String where = "[ " + NotiServiceCall.log.getName() + " create]";
        return circuitBreakerFactory.create("createNoti").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = notiServiceFeignClient.createObject(object); //data: Notification
                    log.info("response " + where + ": " + responseEntity);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        return true;
                    } else {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        if(responseObject != null){
                            throw new RuntimeException(responseObject.getMessage());
                        }
                        return false;
                    }
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return false;
                });
    }
}
