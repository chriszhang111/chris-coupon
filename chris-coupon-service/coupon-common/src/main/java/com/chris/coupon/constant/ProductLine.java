package com.chris.coupon.constant;

//产品线枚举

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ProductLine {

    DM("DM", 1),
    DB("DB", 2);

    private String description;

    private Integer code;

    public static ProductLine of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values()).
                filter(bean -> bean.code.equals(code)).
                findAny().
                orElseThrow(()->new IllegalArgumentException(code + "not exist"));
    }
}
