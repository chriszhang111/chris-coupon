package com.chris.coupon.executor.impl;

import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.executor.AbstractExecutor;
import com.chris.coupon.executor.RuleExecutor;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * <h1>折扣优惠券执行器</h1>
 *
 * */
@Component
@Slf4j
@SuppressWarnings("all")
public class DiscountExecutor extends AbstractExecutor implements RuleExecutor{

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        SettlementInfo probility = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if(probility != null){
            log.debug("ZHEKOU template is not match goods type");
            return probility;
        }
        //折扣优惠券可以直接使用
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();
        double base = (double)templateSDK.getRule().getDiscount().getBase();
        if(goodsSum < base){
            log.debug("Current Goods Cost Sum < Discount Coupon Base");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        //计算使用优惠券之后的价格
        settlement.setCost(
                retain2Decimals(Math.max(goodsSum*(quota*1.0/100), minCost()))
        );
        log.debug("USE ZHEKOU coupon from {} to {}", goodsSum, settlement.getCost());
        return settlement;
    }
}
