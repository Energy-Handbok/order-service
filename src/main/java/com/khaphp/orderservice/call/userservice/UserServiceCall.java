package com.khaphp.orderservice.call.userservice;

import com.khaphp.common.entity.UserSystem;
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
public class UserServiceCall {
    public static final String CALL_OTHER_SERVICE_ERROR = "Call other service error: {}";
    public static final String CALL_OTHER_SERVICE = "Call other service";
    private final UserServiceFeignClient userServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    public UserSystem getObject(String id) {
        return (UserSystem) circuitBreakerFactory.create("getdetail").run(
                () -> {
                    log.info("[getObject]" + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = userServiceFeignClient.getObject(id);
                    log.info("response [getObject]: " + responseEntity);
                    return getUserFromResponse(responseEntity);
                },
                throwable -> {
                    log.error("[getObject]" + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return null;
                });
    }

    private UserSystem getUserFromResponse(ResponseEntity<?> responseEntity) {
        UserSystem user = null;
        try {
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) responseEntity.getBody();
            if(data == null) return null;
            user = UserSystem.linkedHashMapToEntity((LinkedHashMap<String, Object>) data.get("data"));
        } catch (NullPointerException | ParseException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public UserSystem getDetailByEmail(String email){
        return (UserSystem) circuitBreakerFactory.create("getdetailByEmail").run(
                () -> {
                    log.info("[getDetailByEmail]" + CALL_OTHER_SERVICE);
                    ResponseEntity<?> responseEntity = userServiceFeignClient.getObjectByEmail(email);
                    log.info("response [getDetailByEmail]: " + responseEntity);
                    return getUserFromResponse(responseEntity);
                },
                throwable -> {
                    log.error("[getDetailByEmail]" + CALL_OTHER_SERVICE_ERROR, throwable.getMessage());
                    return null;
                });
    }
}
