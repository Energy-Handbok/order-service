package com.khaphp.orderservice.call.food;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.entity.Food;
import com.khaphp.orderservice.call.notiservice.NotiServiceCall;
import com.khaphp.orderservice.constant.StatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceCall {
    public static final String CALL_OTHER_SERVICE_ERROR = "Call other service error: {}";
    public static final String CALL_OTHER_SERVICE = "Call other service";
    private final FoodServiceFeignClient foodServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Food getDetail(String id) {
        String where = "[ " + FoodServiceCall.log.getName() + " detail]";
        return circuitBreakerFactory.create("getDetailFood").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = foodServiceFeignClient.getDetail(id); //data: Food
                    log.info("response " + where + ": " + responseEntity);
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        if (responseObject != null) {
                            throw new RuntimeException(responseObject.getMessage());
                        }
                    }
                    try {
                        return getObjectFromResponse((LinkedHashMap<String, Object>) responseEntity.getBody());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return null;
                });
    }

//    public ResponseEntity<Object> getDetail2(String id) {
//        String where = "[ " + FoodServiceCall.log.getName() + " detail]";
//        return (ResponseEntity<Object>) circuitBreakerFactory.create("getDetailFood2").run(
//                () -> {
//                    log.info(where + CALL_OTHER_SERVICE);
//                    ResponseEntity<?> responseEntity = foodServiceFeignClient.getDetail(id); //data: Food
//                    log.info("response " + where + ": " + responseEntity);
//                    return responseEntity;
//                },
//                throwable -> {
//                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
//                    return null;
//                });
//    }

    public String update(FoodDTOupdate object) {
        String where = "[ " + FoodServiceCall.log.getName() + " update]";
        return (String)circuitBreakerFactory.create("updateFood").run(
                () -> {
                    log.info(where + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = foodServiceFeignClient.updateObject(object); //data: Food
                    log.info("response " + where + ": " + responseEntity);
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        ResponseObject responseObject = (ResponseObject) responseEntity.getBody();
                        if (responseObject != null) {
                            throw new RuntimeException(responseObject.getMessage());
                        }
                    }
                    return StatusResponse.SUCCESS.toString();
                },
                throwable -> {
                    log.error(where + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return StatusResponse.FAILURE.toString();
                });
    }

    private Food getObjectFromResponse(LinkedHashMap<String, Object> body) throws ParseException {
        return Food.mapFromLinkedHashMap((LinkedHashMap<String, Object>) body.get("data"));
    }
}
