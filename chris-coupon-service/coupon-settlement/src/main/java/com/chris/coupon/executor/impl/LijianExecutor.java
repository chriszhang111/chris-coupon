package com.chris.coupon.executor.impl;

import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.executor.AbstractExecutor;
import com.chris.coupon.executor.RuleExecutor;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h1>立减执行器</h1>
 * */

@Component
@Slf4j
public class LijianExecutor extends AbstractExecutor implements RuleExecutor{

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodSum = goodsCostSum(settlement.getGoodsInfos());
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodSum);
        if(probability != null){
            log.debug("LIJIAN coupon is not macth");
            return probability;
        }
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();
        settlement.setCost(retain2Decimals(
                Math.max(goodSum-quota, minCost())
        ));
        log.debug("Use LIJIAN coupon makes goods cost from {} to {}", goodSum, settlement.getCost());
        return settlement;
    }
}
