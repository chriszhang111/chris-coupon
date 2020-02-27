package com.chris.coupon.executor.impl;


import com.alibaba.fastjson.JSON;
import com.chris.coupon.constant.CouponCategory;
import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.executor.AbstractExecutor;
import com.chris.coupon.executor.RuleExecutor;
import com.chris.coupon.vo.GoodsInfo;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>满减+折扣优惠券结算规则处理器</h1>
 * */
@Component
@Slf4j
public class ManjianAndDiscountExector extends AbstractExecutor implements RuleExecutor{

    @Override
    protected boolean isGoodsTypeSatisfiy(SettlementInfo settlement) {
        log.debug("Check ManJian and Zhekou is match or not");
        //结算信息中， 所有商品的类型
        List<Integer> goodsType = settlement.getGoodsInfos().stream().map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().forEach(ct->{
            templateGoodsType.addAll(JSON.parseObject(ct.getTemplate().getRule().getUsage().getGoodsType(), List.class));
        });

        Set<Integer> goodsTypeSet = new HashSet<>(goodsType);
        Set<Integer> templateGoodsTypeSet = new HashSet<>(templateGoodsType);
        return CollectionUtils.isSubCollection(goodsTypeSet, templateGoodsTypeSet);
    }

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if(probability != null){
            log.debug("ManJian and Zhekou template is not match to goods type");
            return probability;
        }

        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zhekou = null;
        for(SettlementInfo.CouponAndTemplateInfo ct: settlement.getCouponAndTemplateInfos()){
            if(CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.MANJIAN){
                manJian = ct;
            }else if(CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.ZHEKOU)
                zhekou = ct;
        }

        assert manJian != null && zhekou != null;
        //当前的优惠券如果不能共用， 清空优惠券返回商品原价
        /*
        if(!isTemplateCanShared(manJian, zhekou)){
            log.debug("Current Manjian and Zhekou can not shared");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }*/

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manJianBase = (double) manJian.getTemplate().getRule().getDiscount().getBase();
        double manJiamQuota = (double)manJian.getTemplate().getRule().getDiscount().getQuota();

        double targetSum = goodsSum;
        if(targetSum >= manJianBase){
            targetSum -= manJiamQuota;
            ctInfos.add(manJian);
        }

        //double zhekouBase = (double)zhekou.getTemplate().getRule().getDiscount().getBase();
        double zhekouQuota = (double)zhekou.getTemplate().getRule().getDiscount().getQuota();
        targetSum *= zhekouQuota * 1.0 /100;
        ctInfos.add(zhekou);
        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(retain2Decimals(Math.max(targetSum, minCost())));
        log.debug("Use ManJian and Zhekou Coupon make goods cost from {} to {}", goodsSum, settlement.getCost());
        return settlement;

    }

    /**
     * <h2>校验当前两张优惠券是否可以共用</h2>
     * 即校验TemplateRule中的weight 是否满足
     * */
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                                        SettlementInfo.CouponAndTemplateInfo zhekou){
            String manjianKey = manJian.getTemplate().getKey()+String.format("%04d", manJian.getTemplate().getId());
            String zhekouKey = zhekou.getTemplate().getKey()+String.format("%04d", zhekou.getTemplate().getId());

            List<String> allSharedKeysForManJian = new ArrayList<>();
            allSharedKeysForManJian.add(manjianKey);
            allSharedKeysForManJian.addAll(JSON.parseObject(
                    manJian.getTemplate().getRule().getWeight(), List.class
            ));

            List<String> allSharedKeysForZhekou = new ArrayList<>();
            allSharedKeysForZhekou.add(zhekouKey);
            allSharedKeysForZhekou.addAll(JSON.parseObject(
                    zhekou.getTemplate().getRule().getWeight(), List.class
            ));

            return CollectionUtils.isSubCollection(Arrays.asList(manjianKey, zhekouKey),
                    allSharedKeysForManJian) || CollectionUtils.isSubCollection(Arrays.asList(manjianKey, zhekouKey), allSharedKeysForZhekou);

    }
}
