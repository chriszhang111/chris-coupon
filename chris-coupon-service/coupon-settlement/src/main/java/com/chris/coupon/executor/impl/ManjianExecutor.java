package com.chris.coupon.executor.impl;


import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.executor.AbstractExecutor;
import com.chris.coupon.executor.RuleExecutor;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 *
 * 满减券结算规则执行器 */
@Component
@Slf4j
public class ManjianExecutor extends AbstractExecutor implements RuleExecutor{

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals(
                goodsCostSum(settlement.getGoodsInfos())
        );

        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if(probability != null){
            log.debug("MANJIAN template is not Match!!!");
            return probability;
        }
        //判断满减是否符合折扣标准, couponAndTemplateInfos只包含一张优惠券
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double base = (double)templateSDK.getRule().getDiscount().getBase();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();  //

        //如果不符合标准，则直接返回商品总价
        if(goodsSum < base){
            log.debug("Current Goods Cost Sum < ManJian Coupon Base");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        //计算使用优惠券之后的价格
        settlement.setCost(retain2Decimals(
                (goodsSum-quota) > minCost() ? (goodsSum-quota) : minCost()
        ));
        log.debug("Use MANJIAN Coupon makes goods cost from {} to {}", goodsSum, settlement.getCost());
        return settlement;
    }
}
