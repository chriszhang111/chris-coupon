package com.chris.coupon.service;

import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.vo.TemplateRequest;

public interface IBuildTemplateService {

    CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException;
}
