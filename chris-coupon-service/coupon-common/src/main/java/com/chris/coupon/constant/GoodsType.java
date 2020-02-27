package com.chris.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>商品类型</h1>
 *
 * */
@Getter
@AllArgsConstructor
public enum GoodsType {

    MEDIA("文娱", 1),
    FRESH("生鲜", 2),
    DAILY("家居", 3),
    OTHERS("其他",4),
    MEAT("肉类", 5),
    FRUIT("水果", 6),
    ALL_PRODUCT("所有商品", Integer.MAX_VALUE);

    private String desc;
    private Integer code;

    public static GoodsType of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException("Code not Exists"));
    }
}
