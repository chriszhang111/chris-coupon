package com.chris.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.executor.ExecuteManager;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>结算服务controller</h1>
 * */
@RestController
@Slf4j
public class SettlementController {

    private final ExecuteManager executeManager;

    @Autowired
    public SettlementController(ExecuteManager executeManager) {
        this.executeManager = executeManager;
    }

    /**
     * <h2>优惠券结算</h2>
     * */
    @PostMapping(value = "/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement)
        throws CouponException{
        log.info("Settlement:{}", JSON.toJSONString(settlement));
        return executeManager.computeRule(settlement);
    }
}
