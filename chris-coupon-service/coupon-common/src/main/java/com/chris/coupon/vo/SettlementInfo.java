package com.chris.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>结算信息</h1>
 * 1.userId
 * 2.商品信息
 * 3. 优惠券列表
 * 4. 结算结果金额
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {


    private Long userId;
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;
    private List<GoodsInfo> goodsInfos;

    private Boolean employ; // 是否使结算生效

    private Double cost;  //结果结算金额


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponAndTemplateInfo{
        private Integer id;

        private CouponTemplateSDK template;
    }


}
