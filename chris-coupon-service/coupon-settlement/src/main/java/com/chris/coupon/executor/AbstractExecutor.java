package com.chris.coupon.executor;


import com.alibaba.fastjson.JSON;
import com.chris.coupon.vo.GoodsInfo;
import com.chris.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * <h1>规则执行器抽象类</h1>*/
public abstract class AbstractExecutor {

    /**
     * <h1>校验商品类型与优惠券是否匹配</h1>
     * @notice： 单品类优惠券校验， 多品类时重载
     *           商品只需要有一个优惠券要求的商品类型去匹配，单品类 couponAndTemplateInfos 只包含一张优惠券
     * */
    protected boolean isGoodsTypeSatisfiy(SettlementInfo settlement){
        List<Integer> goodsType = settlement.getGoodsInfos()
                .stream().map(GoodsInfo::getType).collect(Collectors.toList());


        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0).getTemplate()
                .getRule().getUsage().getGoodsType(), List.class
        );

        Set<Integer> goodsTypeSet = new HashSet<>(goodsType);
        Set<Integer> templateGoodsTypeSet = new HashSet<>(templateGoodsType);

        return CollectionUtils.isSubCollection(goodsTypeSet, templateGoodsTypeSet);

    }

    /**
     * <h1>商品类型与优惠券限制不匹配的情况</h1>
     *
     * */
    protected SettlementInfo processGoodsTypeNotSatisfy(
            SettlementInfo settlementInfo, double goodsSum
    ){
        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfiy(settlementInfo);

        //当商品类型不满足时，直接返回总价并清空优惠券
        if(!isGoodsTypeSatisfy){
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }

        return null;
    }

    /**
     * <h2>商品总价</h2>
     * */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos){
        return goodsInfos.stream().mapToDouble(
                g->g.getPrice()*g.getCount()
        ).sum();
    }

    protected double retain2Decimals(double value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    protected double minCost(){
        return 0.1;
    }


}
