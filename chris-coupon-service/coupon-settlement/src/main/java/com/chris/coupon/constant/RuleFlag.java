package com.chris.coupon.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>规则类型</h1>
 * */

@Getter
@AllArgsConstructor
public enum RuleFlag {
    //单类别优惠券
    MANJIAN("rule of manjian"),    //满减券，需要满足金额数量
    ZHEKOU("rule of discount"),    // 打多少折扣
    LIJIAN("rule of immediate discount"),  //直接减多少

    //多类别定义
    MANJIAN_ZHEKOU("manjian plus discount"),
    ZHEKOU_LIJIAN("discount plus immediate");

    private String description;




}
