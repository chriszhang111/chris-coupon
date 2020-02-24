package com.chris.coupon.service;

import com.chris.coupon.Entity.CouponTemplate;

/**
 * <h1>异步服务接口定义</h1>
 */

public interface IAsyncService {

    /**
     * <h2>根据模版异步创建优惠券码</h2>
     * */
    void asyncConstructCouponByTemplate(CouponTemplate template);


}
