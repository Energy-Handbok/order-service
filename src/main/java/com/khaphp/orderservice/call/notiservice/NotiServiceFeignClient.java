package com.khaphp.orderservice.call.notiservice;

import com.khaphp.common.dto.noti.NotificationDTOcreate;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotiServiceFeignClient {
    @PostMapping("/api/v1/notification")
    public ResponseEntity<Object> createObject(@RequestBody @Valid NotificationDTOcreate object);
}
