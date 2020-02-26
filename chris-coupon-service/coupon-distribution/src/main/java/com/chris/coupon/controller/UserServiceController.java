package com.chris.coupon.controller;


import com.alibaba.fastjson.JSON;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.entity.Coupon;
import com.chris.coupon.service.IUserService;
import com.chris.coupon.vo.AcquireTemplateRequest;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h1>用户服务controller</h1>
 * */
@Slf4j
@RestController
public class UserServiceController {

    private IUserService userService;

    @Autowired
    public UserServiceController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/coupons")
    public List<Coupon> findCouponsByStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status
    ) throws CouponException{
        log.info("Find Coupons By Status: {}, {}", userId, status);
        return userService.findCouponsByStatus(userId, status);
    }


    @GetMapping("/template")
    public List<CouponTemplateSDK> findAvailableTemplate(
            @RequestParam("userId") Long userId
    ) throws CouponException{
        log.info("Find Available Template: {}", userId);
        return userService.findAvailableTemplate(userId);
    }

    @PostMapping("/acquire/template")
    public Coupon AcquireTemplate(
            @RequestBody AcquireTemplateRequest request
    ) throws CouponException{
        log.info("Acquire Template:{}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }


    @PostMapping("/settlement")
    public SettlementInfo settlement(@RequestBody SettlementInfo info) throws CouponException{
        log.info("Settlement: {}", JSON.toJSONString(info));
        return userService.settlement(info);
    }
}
