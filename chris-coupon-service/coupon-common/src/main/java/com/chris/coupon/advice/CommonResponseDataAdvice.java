package com.chris.coupon.advice;

import com.chris.coupon.annotation.IngoreResponseAdvice;
import com.chris.coupon.vo.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class CommonResponseDataAdvice implements ResponseBodyAdvice<Object>{

    //判断是否需要对响应处理
    @Override
    @SuppressWarnings("all")
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        //如果当前方法所在的类标示了@IgnoreResponseAdvice， 不需要处理
        if(methodParameter.getDeclaringClass().isAnnotationPresent(
                IngoreResponseAdvice.class
        )){
            return false;
        }

        if(methodParameter.getMethod().isAnnotationPresent(IngoreResponseAdvice.class)){
            return false;
        }

        //对响应处理， 执行beforeBodyWrite
        return true;
    }



    //响应返回前的处理
    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        CommonResponse<Object> response = new CommonResponse<>(0, "");
        if(o == null){
            return response;
        }else if(o instanceof CommonResponse){
            response = (CommonResponse<Object>) o;
        }else{
            response.setData(o);
        }
        return response;
    }
}
