package com.khaphp.orderservice.util.valid.MethodOrder;

import com.khaphp.orderservice.constant.Method;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidMethodValidator implements ConstraintValidator<ValidMethod, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> roles = List.of(Method.COD.name(), Method.WALLET.name(), Method.THIRDPARTY.name());
        if(roles.contains(value)){
            return true;
        }else{
            return false;
        }
    }
}
