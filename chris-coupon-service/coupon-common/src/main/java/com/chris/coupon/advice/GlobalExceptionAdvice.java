package com.chris.coupon.advice;

import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(HttpServletRequest req,
                                                         CouponException ex){
        CommonResponse<String> response = new CommonResponse<>(-1, "Business Error");
        response.setData(ex.getMessage());
        return response;
    }
}
