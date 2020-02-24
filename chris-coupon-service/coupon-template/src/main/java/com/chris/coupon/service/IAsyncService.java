package com.chris.coupon.service;

import com.chris.coupon.Entity.CouponTemplate;

public interface IAsyncService {

    void asyncConstructCouponByTemplate(CouponTemplate template);
}
