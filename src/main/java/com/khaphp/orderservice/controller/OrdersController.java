package com.khaphp.orderservice.controller;

import com.khaphp.common.dto.ResponseObject;
import com.khaphp.orderservice.dto.Order.OrderDTOcreate;
import com.khaphp.orderservice.dto.Order.OrderDTOupdate;
import com.khaphp.orderservice.dto.Order.ParamCancelOrder;
import com.khaphp.orderservice.dto.Order.ParamOrderShipperId;
import com.khaphp.orderservice.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/order")
//@SecurityRequirement(name = "EnergyHandbook")
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersService ordersService;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(defaultValue = "1") int pageIndex){
        ResponseObject<Object> responseObject = ordersService.getAll(pageSize, pageIndex);
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }
    @GetMapping("/detail")
    public ResponseEntity<Object> getObject(String id){
        ResponseObject<Object> responseObject = ordersService.getDetail(id);
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @PostMapping
    @Operation(summary = "COD (guest, cus), WALLET (GUEST)",
    description = "nếu guest thì khỏi truyền customerId, còn nếu cus thìkho3oi3 truyền name, phone, address guest về")
    public ResponseEntity<Object> createObject(@RequestBody @Valid OrderDTOcreate object) throws Exception {
        try{
            ResponseObject<Object> responseObject = ordersService.create(object);
            if(responseObject.getCode() == 200){
                return ResponseEntity.ok(responseObject);
            }
            return ResponseEntity.badRequest().body(responseObject);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .code(400).message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/third-party")
    @Operation(
            summary = "THIRD PARTY (guest, cus)",
            description = "- nếu guest thì khỏi truyền customerId                        \n" +
                    "- còn nếu cus thì khỏi truyền name, phone, address guest về")
    public ResponseEntity<Object> createObject3Party(HttpServletRequest request, @RequestBody @Valid OrderDTOcreate object) throws Exception {
        try{
            ResponseObject<Object> responseObject = ordersService.orderThirdParty(request, object);
            if(responseObject.getCode() == 200){
                return ResponseEntity.ok(responseObject);
            }
            return ResponseEntity.badRequest().body(responseObject);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .code(400).message(e.getMessage())
                    .build());
        }
    }

    @PutMapping("/status")
    public ResponseEntity<Object> updateObjectStatus(@RequestBody @Valid OrderDTOupdate object){
        ResponseObject<Object> responseObject = ordersService.updateStatus(object);
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @PutMapping("/shipper-take")
    public ResponseEntity<Object> updateShipperTakeOrder(@RequestBody @Valid ParamOrderShipperId object){
        ResponseObject<Object> responseObject = ordersService.updateShipperTakeOrder(object.getOrderId(), object.getShipperId());
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @PutMapping("/finish-delivery")
    public ResponseEntity<Object> finishDeilivery(@RequestBody @Valid ParamOrderShipperId object){
        ResponseObject<Object> responseObject = ordersService.finishDeilivery(object.getOrderId(), object.getShipperId());
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @PutMapping("/cancel")
    @Operation(description = "nếu ai là người hủy thì điển role + id của ng đó vào thôi")
    public ResponseEntity<Object> cancelOrder(@RequestBody @Valid ParamCancelOrder object){
        ResponseObject<Object> responseObject = ordersService.cancelOrder(object.getOrderId(), object.getUserId(), object.getRole());
        if(responseObject.getCode() == 200){
            return ResponseEntity.ok(responseObject);
        }
        return ResponseEntity.badRequest().body(responseObject);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteObject(String id){
        try{
            ResponseObject<Object> responseObject = ordersService.delete(id);
            if(responseObject.getCode() == 200){
                return ResponseEntity.ok(responseObject);
            }
            return ResponseEntity.badRequest().body(responseObject);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .code(400).message(e.getMessage())
                    .build());
        }
    }
}
