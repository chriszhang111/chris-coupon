package com.chris.coupon.controller;

import com.chris.coupon.annotation.IngoreResponseAdvice;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * <h1>Ribbon 应用Controller</h1>
 *
 * */
@Slf4j
@RestController
public class RibbonController {

    private RestTemplate restTemplate;

    @Autowired
    public RibbonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     *
     * */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TemplateInfo{
        private Integer code;
        private String message;
        private List<Map<String, Object>> data;
    }


    // coupon-distribution/info
    // http://127.0.0.1:9000/chris/coupon-distribution/info
    @GetMapping("/info")
    @IngoreResponseAdvice
    public TemplateInfo getTemplateInfo(){

        String infoUrl = "http://eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity(infoUrl,
                TemplateInfo.class).getBody();
    }


}
