package com.chris.coupon.service.impl;

import com.chris.coupon.Dao.CouponTemplateDao;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.service.IAsyncService;
import com.chris.coupon.service.IBuildTemplateService;
import com.chris.coupon.vo.TemplateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService{

    private final IAsyncService asyncService;
    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        return null;
    }
}
