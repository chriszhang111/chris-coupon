package com.chris.coupon.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

//分发目标
@Getter
@AllArgsConstructor
public enum DistributeTarget {

    SINGLE("single user", 1),
    MULTI("multi user", 2);

    private String desrciption;
    private Integer code;

    public static DistributeTarget of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values()).
                filter(bean -> bean.code.equals(code)).
                findAny().
                orElseThrow(()->new IllegalArgumentException(code + "not exist"));
    }
}
