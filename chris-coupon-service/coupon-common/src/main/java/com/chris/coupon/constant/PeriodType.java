package com.chris.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

//有效期类型
/*
*
*
* */
@Getter
@AllArgsConstructor
public enum PeriodType {

    REGULAR("固定日期", 1),
    SHIFT("变动（领取时开始计算）", 2);

    private String description;
    private Integer code;

    public static PeriodType of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values()).
                filter(bean -> bean.code.equals(code)).
                findAny().
                orElseThrow(()->new IllegalArgumentException(code + "not exist"));
    }
}
