package com.imooc.web.util;

import com.imooc.web.resp.ValidResult;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:50
 **/
public class ValidUtils {
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private ValidUtils() {
    }

    public static <T> ValidResult valid(T object, Class... groups) {
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> results = validator.validate(object, groups);
        if (results.isEmpty()) {
            return null;
        } else {
            ValidResult respMsg = new ValidResult();
            respMsg.setCheckErrors(new ArrayList());
            StringBuilder errorProperty = new StringBuilder();
            Iterator var6 = results.iterator();

            while(var6.hasNext()) {
                ConstraintViolation<T> constraintViolation = (ConstraintViolation)var6.next();
                String errorCol = constraintViolation.getPropertyPath().toString();
                String errorDetail = constraintViolation.getMessage();
                errorProperty.append(errorCol).append("|");
                ValidResult.CheckError checkError = new ValidResult.CheckError(errorCol, errorDetail);
                respMsg.getCheckErrors().add(checkError);
            }

            String errorPropertyStr = errorProperty.toString();
            if (errorProperty.length() > 0) {
                errorPropertyStr = errorProperty.substring(0, errorProperty.length() - 1);
            }

            respMsg.setDescription("Check failed, the [" + errorPropertyStr + "] is not appropriate.");
            return respMsg;
        }
    }
}
