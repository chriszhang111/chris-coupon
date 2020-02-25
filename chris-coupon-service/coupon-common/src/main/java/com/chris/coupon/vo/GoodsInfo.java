package com.chris.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {

    /** {@link com.chris.coupon.constant.GoodsType}
    *
    * */
    private Integer type;

    private Double price;

    private Integer count;  //number of goods

    private String name;

    //TODO name, info...


}
