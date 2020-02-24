package com.chris.coupon.controller;

import com.chris.coupon.Exception.CouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * <h1>健康检查接口</h1>
 * */
@Slf4j
@RestController
public class HealthCheck {


    /* 服务发现客户端 */
    private final DiscoveryClient client;

    /* 服务注册接口 */
    private final Registration registration;

    @Autowired
    public HealthCheck(DiscoveryClient client, Registration registration) {
        this.client = client;
        this.registration = registration;
    }

    /***
     * 127.0.0.1: 7001/coupon-template/health
     *
     */
    @GetMapping("/health")
    public String health(){
        log.debug("view health api");
        return "CouponTemplate Is OK";
    }

    @GetMapping("/exception")
    public String exception() throws CouponException{
        log.debug("View exception api");
        throw new CouponException("CouponTemplate Has some Problem");
    }


    @GetMapping("/info")
    public List<Map<String, Object>> info(){
        List<ServiceInstance> instances =
                client.getInstances(registration.getServiceId());
        List<Map<String, Object>> result = new ArrayList<>(instances.size());

        instances.forEach(i -> {
            Map<String, Object> info = new HashMap<>();
            info.put("serviceId", i.getServiceId());
            info.put("instanceId", i.getInstanceId());
            info.put("port", i.getPort());
            info.put("uri",i.getUri());
            result.add(info);
        });

        return result;
    }
}
