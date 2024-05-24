package com.khaphp.orderservice.util.valid.StatusOrder;

import com.khaphp.orderservice.constant.StatusOrder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidStatusOrderValidator implements ConstraintValidator<ValidStatusOrder, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> roles = List.of(StatusOrder.PENDING.toString(), StatusOrder.ACCEPT.toString(), StatusOrder.REJECT.toString(), StatusOrder.CANCEL.toString(), StatusOrder.WAITING.toString(), StatusOrder.FINISH.toString());
        if(roles.contains(value)){
            return true;
        }else{
            return false;
        }
    }
}
