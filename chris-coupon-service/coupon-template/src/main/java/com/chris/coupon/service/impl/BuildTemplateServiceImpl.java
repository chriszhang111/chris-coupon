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

/**
 * <h1>优惠券模版接口实现</h1>
 * */
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
        if(!request.validate()){
            throw new CouponException("Build Template Error, Param is not valid");
        }

        if(null != templateDao.findByName(request.getName())){
            throw new CouponException("Exist same name template");
        }

        //construct coupontemplate and save

        CouponTemplate template = requestToTemplate(request);
        template = templateDao.save(template);

        //异步生成优惠券码  1个优惠券模版 多个优惠券码
        this.asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    private CouponTemplate requestToTemplate(TemplateRequest request){
        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }

}
