package com.chris.coupon.vo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>获取优惠券请求对象定义</h1>
 * Created by Qinyi.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcquireTemplateRequest {

    private Long userId;

    /** 优惠券模板信息 */
    private CouponTemplateSDK templateSDK;
}
