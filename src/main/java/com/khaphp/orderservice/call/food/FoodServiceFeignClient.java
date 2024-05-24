package com.khaphp.orderservice.call.food;

import com.khaphp.common.dto.food.FoodDTOupdate;
import com.khaphp.common.dto.noti.NotificationDTOcreate;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "food-service")
public interface FoodServiceFeignClient {
    @GetMapping("/api/v1/food/detail")
    public ResponseEntity<Object> getDetail(@RequestParam String id);
    @PutMapping("/api/v1/food")
    public ResponseEntity<Object> updateObject(@RequestBody @Valid FoodDTOupdate object);
}
